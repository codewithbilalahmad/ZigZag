package com.muhammad.zigzag.presentation.screens.whiteboard

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.muhammad.zigzag.domain.model.DrawingTool
import com.muhammad.zigzag.domain.model.DrawnPath
import com.muhammad.zigzag.domain.model.WhiteBoard
import com.muhammad.zigzag.domain.repository.PathRepository
import com.muhammad.zigzag.domain.repository.SettingRepository
import com.muhammad.zigzag.domain.repository.WhiteBoardRepository
import com.muhammad.zigzag.presentation.navigation.Destinations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.muhammad.canvos.domain.model.ColorPaletteType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class WhiteboardViewModel(
    private val pathRepository: PathRepository,
    private val whiteBoardRepository: WhiteBoardRepository,
    private val settingRepository: SettingRepository, savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val whiteBoardId = savedStateHandle.toRoute<Destinations.WhiteBoardScreen>().whiteBoardId
    private var isFirstPath = true
    private var updatedWhiteBoardId = MutableStateFlow(whiteBoardId)
    private val _state = MutableStateFlow(WhiteboardState())
    val state = combine(
        _state,
        settingRepository.getPreferredStrokeColors(),
        settingRepository.getPreferredFillColors(),
        settingRepository.getPreferredCanvasColors()
    ) { state, prefStrokeColors, prefFillColors, prefCanvasColor ->
        state.copy(
            preferredFillColors = prefFillColors,
            preferredStrokeColors = prefStrokeColors,
            preferredCanvasColor = prefCanvasColor
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WhiteboardState()
    )

    init {
        whiteBoardId?.let { id ->
            getWhiteBoardById(id)
        }
        observePaths()
    }

    private fun getWhiteBoardById(whiteBoardId: Long) {
        println("Whiteboard id : $whiteBoardId")
        viewModelScope.launch {
            val whiteboard = whiteBoardRepository.getWhiteBoardById(whiteBoardId)
            whiteboard?.let {
                _state.update {
                    it.copy(
                        whiteBoardName = it.whiteBoardName,
                        canvasColor = it.canvasColor
                    )
                }
            }
        }
    }

    fun onEvent(event: WhiteboardEvent) {
        when (event) {
            WhiteboardEvent.OnDrawingToolClose -> {
                _state.update { it.copy(isDrawingToolsCardVisible = false) }
            }

            is WhiteboardEvent.BackgroundColorChange -> {
                _state.update {
                    it.copy(backgroundColor = event.backgroundColor)
                }
            }

            is WhiteboardEvent.CanvasColorChange -> {
                _state.update { it.copy(canvasColor = event.canvasColor) }
                upsertWhiteBoard()
            }

            is WhiteboardEvent.ContinueDrawing -> {
                updateContinueOffsets(event.offset)
            }

            WhiteboardEvent.FinishDrawing -> {
                state.value.currentPath?.let { drawnPath ->
                    when (drawnPath.drawnPath) {
                        DrawingTool.ERASER -> {
                            deletePaths(state.value.pathToBeDeleted)
                        }

                        DrawingTool.LASER_PEN -> {
                            _state.update { it.copy(laserPenPath = drawnPath) }
                        }

                        else -> {
                            insertPath(drawnPath)
                        }
                    }
                }
                _state.update { it.copy(currentPath = null, pathToBeDeleted = emptyList()) }
            }

            is WhiteboardEvent.OnDrawingToolClose -> {
                _state.update {
                    it.copy(isDrawingToolsCardVisible = false)
                }
            }

            is WhiteboardEvent.OnDrawingToolSelected -> {
                when (event.drawingTool) {
                    DrawingTool.RECTANGLE, DrawingTool.CIRCLE, DrawingTool.TRIANGLE -> {
                        _state.update {
                            it.copy(selectedDrawingTool = event.drawingTool)
                        }
                    }

                    else -> {
                        _state.update {
                            it.copy(
                                selectedDrawingTool = event.drawingTool,
                                backgroundColor = Color.Transparent
                            )
                        }
                    }
                }
            }

            WhiteboardEvent.OnFabClick -> {
                _state.update { it.copy(isDrawingToolsCardVisible = true) }
            }

            is WhiteboardEvent.OpacitySlideValueChange -> {
                _state.update {
                    it.copy(opacity = event.opacity)
                }
            }

            is WhiteboardEvent.StartDrawing -> {
                if (isFirstPath) {
                    upsertWhiteBoard()
                    isFirstPath = false
                }
                _state.update {
                    it.copy(startingOffset = event.offset)
                }
            }

            is WhiteboardEvent.StrokeColorChange -> {
                _state.update {
                    it.copy(strokeColor = event.strokeColor)
                }
            }

            is WhiteboardEvent.StrokeSlideValueChange -> {
                _state.update {
                    it.copy(strokeWidth = event.strokeWidth)
                }
            }

            WhiteboardEvent.OnLaserPathAnimationComplete -> {
                _state.update { it.copy(laserPenPath = null) }
            }

            is WhiteboardEvent.OnColorPaletteIconClick -> {
                _state.update {
                    it.copy(
                        isColorSelectionDialogOpen = true,
                        selectedColorPaletteType = event.colorPalatteType
                    )
                }
            }

            WhiteboardEvent.ColorSelectionDialogDismiss -> {
                _state.update { it.copy(isColorSelectionDialogOpen = false) }
            }

            is WhiteboardEvent.OnColorSelected -> {
                val state = state.value
                val color = event.color
                val updatedColors = addColorToPreferredList(
                    newColor = color, colors = when (state.selectedColorPaletteType) {
                        ColorPaletteType.CANVAS -> state.preferredCanvasColor
                        ColorPaletteType.STROKE -> state.preferredStrokeColors
                        ColorPaletteType.FILL -> state.preferredFillColors
                    }
                )
                when (state.selectedColorPaletteType) {
                    ColorPaletteType.CANVAS -> {
                        _state.update { it.copy(canvasColor = color) }
                        upsertWhiteBoard()
                    }

                    ColorPaletteType.STROKE -> {
                        _state.update { it.copy(strokeColor = color) }
                    }

                    ColorPaletteType.FILL -> {
                        _state.update { it.copy(fillColor = color) }
                    }
                }
                savePreferredColors(updatedColors, state.selectedColorPaletteType)
            }

            WhiteboardEvent.OnToggleCommandPaletteBottomSheet -> {
                _state.update { it.copy(showCommandPaletteBottomSheet = !state.value.showCommandPaletteBottomSheet) }
            }
        }
    }

    private fun savePreferredColors(colors: List<Color>, colorPalette: ColorPaletteType) {
        viewModelScope.launch {
            settingRepository.savePreferredColors(colors = colors, colorPaletteType = colorPalette)
        }
    }

    private fun upsertWhiteBoard() {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val whiteBoard = WhiteBoard(
                name = state.value.whiteBoardName,
                lastEdited = today,
                canvasColor = state.value.canvasColor,
                id = updatedWhiteBoardId.value
            )
            val newId = whiteBoardRepository.upsertWhiteboard(whiteBoard)
            updatedWhiteBoardId.value = newId
        }
    }

    private fun deletePaths(paths: List<DrawnPath>) {
        viewModelScope.launch {
            paths.forEach { path ->
                pathRepository.deletePath(path)
            }
        }
    }

    private fun insertPath(path: DrawnPath) {
        viewModelScope.launch {
            pathRepository.upsertPath(path)
        }
    }

    private fun updateContinueOffsets(offset: Offset) {
        val startOffset = state.value.startingOffset
        val updatedPath = when (state.value.selectedDrawingTool) {
            DrawingTool.PEN, DrawingTool.HIGHLIGHTER, DrawingTool.LASER_PEN -> {
                createFreeHandPath(start = startOffset, end = offset)
            }

            DrawingTool.ERASER -> {
                updatePathsToBeDeleted(start = startOffset, continuingOffset = offset)
                createEraserPath(continuingOffset = offset)
            }

            DrawingTool.LINE -> {
                createLinePath(start = startOffset, end = offset)
            }

            DrawingTool.ARROW -> {
                createArrowPath(start = startOffset, end = offset)
            }

            DrawingTool.RECTANGLE -> {
                createRectanglePath(start = startOffset, end = offset)
            }

            DrawingTool.CIRCLE -> {
                createCirclePath(start = startOffset, end = offset)
            }

            DrawingTool.TRIANGLE -> {
                createTrianglePath(start = startOffset, end = offset)
            }
        }
        updatedWhiteBoardId.value?.let { id ->
            _state.update {
                it.copy(
                    currentPath = updatedPath?.let { path ->
                        DrawnPath(
                            path = path,
                            drawnPath = state.value.selectedDrawingTool,
                            strokeWidth = state.value.strokeWidth,
                            backgroundColor = state.value.backgroundColor,
                            opacity = state.value.opacity,
                            strokeColor = state.value.strokeColor, whiteBoardId = id
                        )
                    }
                )
            }
        }
    }

    private fun createEraserPath(continuingOffset: Offset): Path {
        return Path().apply {
            addOval(Rect(center = continuingOffset, radius = 5f))
        }
    }

    private fun createFreeHandPath(start: Offset, end: Offset): Path {
        val existingPath =
            state.value.currentPath?.path ?: Path().apply { moveTo(start.x, start.y) }
        return Path().apply {
            addPath(existingPath)
            lineTo(end.x, end.y)
        }
    }

    private fun createLinePath(start: Offset, end: Offset): Path {
        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)
        }
    }

    private fun createRectanglePath(start: Offset, end: Offset): Path {
        val width = abs(end.x - start.x)
        val height = abs(end.y - start.y)
        return Path().apply {
            addRoundRect(
                roundRect = RoundRect(
                    rect = Rect(
                        offset = start,
                        size = Size(width, height)
                    ), cornerRadius = CornerRadius(8f, 8f)
                )
            )
        }
    }

    private fun createCirclePath(start: Offset, end: Offset): Path {
        val width = abs(end.x - start.x)
        val height = abs(end.y - start.y)
        return Path().apply {
            addOval(Rect(offset = start, size = Size(width, height)))
        }
    }
    private fun createArrowPath(start : Offset, end : Offset) : Path{
        val arrowHeadLength = 30f
        val arrowHeadAngle = 30f
        val angle = atan2(end.y - start.y, end.x - start.x)
        val angleRadius = Math.toRadians(arrowHeadAngle.toDouble()).toFloat()
        val arrowPoint1 = Offset(
            x = end.x - arrowHeadLength * cos(angle - angleRadius),
            y = end.y - arrowHeadLength * sin(angle - angleRadius)
        )
        val arrowPoint2 = Offset(
            x = end.x - arrowHeadLength * cos(angle + angleRadius),
            y = end.y - arrowHeadLength * sin(angle + angleRadius)
        )
        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)
            moveTo(end.x , end.y)
            lineTo(arrowPoint1.x, arrowPoint1.y)
            moveTo(end.x, end.y)
            lineTo(arrowPoint2.x , arrowPoint2.y)
        }
    }

    private fun createTrianglePath(start: Offset, end: Offset): Path {
        val width = abs(end.x - start.y)
        val halfBaseHeight = abs(end.x - start.x) / 2
        val top = start
        val left = Offset(start.x - halfBaseHeight, start.y + halfBaseHeight)
        val right = Offset(start.x + width, start.y + halfBaseHeight)
        val path = Path()
        fun roundedCorner(from: Offset, corner: Offset, to: Offset): Pair<Offset, Offset> {
            val dir1 = (corner - from).normalize()
            val dir2 = (corner - to).normalize()
            val p1 = corner - dir1 * 8f
            val p2 = corner - dir2 * 8f
            return p1 to p2
        }

        val (l1, l2) = roundedCorner(right, top, left)
        val (t1, t2) = roundedCorner(top, left, right)
        val (r1, r2) = roundedCorner(left, right, top)

        path.moveTo(l1.x, l1.y)
        path.quadraticTo(top.x, top.y, l2.x, l2.y)
        path.lineTo(t1.x, t1.y)
        path.quadraticTo(left.x, left.y, t2.x, t2.y)
        path.lineTo(r1.x, r1.y)
        path.quadraticTo(right.x, right.y, r2.x, r2.y)
        path.close()
        return path
    }

    private fun updatePathsToBeDeleted(start: Offset, continuingOffset: Offset) {
        val pathsToBeDeleted = state.value.pathToBeDeleted.toMutableList()
        state.value.paths.forEach { drawnPath ->
            val bounds = drawnPath.path.getBounds()
            if (bounds.contains(start) || bounds.contains(continuingOffset)) {
                if (!pathsToBeDeleted.contains(drawnPath)) {
                    pathsToBeDeleted.add(drawnPath)
                }
            }
        }
        _state.update { it.copy(pathToBeDeleted = pathsToBeDeleted) }
    }

    private fun addColorToPreferredList(
        newColor: Color,
        colors: List<Color>,
    ): List<Color> {
        return listOf(newColor) + colors.filter { it != newColor }.take(3)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePaths() {
        viewModelScope.launch {
            updatedWhiteBoardId.flatMapLatest { id ->
                pathRepository.getPathsForWhiteboard(whiteboardId = id ?: -1)
            }.collectLatest { paths ->
                _state.update { it.copy(paths = paths) }
            }
        }
    }

    private fun Offset.normalize(): Offset {
        val length = getDistance()
        return if (length != 0f) Offset(x = x / length, y / length) else Offset.Zero
    }
}