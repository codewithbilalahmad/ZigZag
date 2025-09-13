package com.muhammad.zigzag.presentation.screens.whiteboard

import android.annotation.SuppressLint
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
import com.muhammad.zigzag.utils.drawWhiteboardThumbnail
import com.muhammad.zigzag.utils.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.muhammad.canvos.domain.model.ColorPaletteType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class WhiteboardViewModel(
    private val pathRepository: PathRepository,
    private val whiteBoardRepository: WhiteBoardRepository,
    private val settingRepository: SettingRepository, savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val whiteBoardId =
        savedStateHandle.toRoute<Destinations.WhiteBoardScreen>().whiteBoardId
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
        viewModelScope.launch {
            val whiteboard = whiteBoardRepository.getWhiteBoardById(whiteBoardId)
            whiteboard?.let { board ->
                _state.update {
                    it.copy(
                        whiteBoardName = board.name,
                        canvasColor = board.canvasColor
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

            WhiteboardEvent.OnDrawingToolFabClick -> {
                _state.update { it.copy(isDrawingToolsCardVisible = !state.value.isDrawingToolsCardVisible) }
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

            WhiteboardEvent.OnToggleCommandPaletteDialog -> {
                _state.update { it.copy(showCommandPaletteDialog = !state.value.showCommandPaletteDialog) }
            }

            WhiteboardEvent.OnRedoPath -> onRedoPath()
            WhiteboardEvent.OnUndoPath -> onUndoPath()
            is WhiteboardEvent.OnCanvasSizeChange -> {
                _state.update { it.copy(canvasSize = event.size) }
            }

            WhiteboardEvent.UpdateWhiteboardPreview -> {
                upsertWhiteBoardPreview()
            }
        }
    }

    private fun onUndoPath() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState.undoStack.isNotEmpty()) {
                val updatedUndo = ArrayDeque(currentState.undoStack)
                val last = updatedUndo.removeLast()
                val updatedRedo = ArrayDeque(currentState.redoStack).apply { addLast(last) }
                _state.update {
                    it.copy(
                        undoStack = updatedUndo,
                        redoStack = updatedRedo,
                        isUndoEnable = updatedUndo.isNotEmpty(),
                        isRedoEnable = true
                    )
                }
            }
        }
    }

    private fun onRedoPath() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState.redoStack.isNotEmpty()) {
                val updatedRedo = ArrayDeque(currentState.redoStack)
                val last = updatedRedo.removeLast()
                val updatedUndo = ArrayDeque(currentState.undoStack).apply { addLast(last) }
                _state.update {
                    it.copy(
                        undoStack = updatedUndo,
                        redoStack = updatedRedo,
                        isUndoEnable = true,
                        isRedoEnable = updatedRedo.isNotEmpty()
                    )
                }
            }
        }
    }

    private fun savePreferredColors(colors: List<Color>, colorPalette: ColorPaletteType) {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.savePreferredColors(colors = colors, colorPaletteType = colorPalette)
        }
    }

    @SuppressLint("UseKtx")
    @OptIn(ExperimentalTime::class)
    private fun upsertWhiteBoardPreview() {
        viewModelScope.launch {
            val originalWidth = state.value.canvasSize.width.takeIf { it > 0 } ?: 1082
            val originalHeight = state.value.canvasSize.height.takeIf { it > 0 } ?: 1920
            val targetWidthPx = 300
            val previewBitmap = drawWhiteboardThumbnail(
                paths = state.value.paths.toList(),
                canvasColor = state.value.canvasColor,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                targetWidthPx = targetWidthPx
            )
            val previewPath = saveBitmapToFile(previewBitmap, updatedWhiteBoardId.value ?: -1)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val updatedWhiteBoard = WhiteBoard(
                name = state.value.whiteBoardName,
                lastEdited = today,
                canvasColor = state.value.canvasColor,
                id = updatedWhiteBoardId.value, previewUrl = previewPath
            )
            val newId = whiteBoardRepository.upsertWhiteboard(updatedWhiteBoard)
            updatedWhiteBoardId.value = newId
        }
    }

    @SuppressLint("UseKtx")
    @OptIn(ExperimentalTime::class)
    private fun upsertWhiteBoard() {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val updatedWhiteBoard = WhiteBoard(
                name = state.value.whiteBoardName,
                lastEdited = today,
                canvasColor = state.value.canvasColor,
                id = updatedWhiteBoardId.value
            )
            val newId = whiteBoardRepository.upsertWhiteboard(updatedWhiteBoard)
            updatedWhiteBoardId.value = newId
        }
    }

    private fun deletePaths(paths: List<DrawnPath>) {
        viewModelScope.launch {
            paths.forEach { path -> pathRepository.deletePath(path) }
            _state.update { state ->
                val idsToDelete = paths.map { it.id }.toSet()
                val newUndoStack = ArrayDeque(state.undoStack.filter { it.id !in idsToDelete })
                val newRedoStack = ArrayDeque(state.redoStack.filter { it.id !in idsToDelete })
                state.copy(
                    undoStack = newUndoStack,
                    redoStack = newRedoStack,
                    isUndoEnable = newUndoStack.isNotEmpty(),
                    isRedoEnable = newRedoStack.isNotEmpty()
                )
            }
        }
    }

    private fun insertPath(path: DrawnPath) {
        viewModelScope.launch {
            pathRepository.upsertPath(path)
            _state.update {
                it.copy(
                    undoStack = ArrayDeque(it.undoStack).apply { addLast(path) },
                    redoStack = ArrayDeque(),
                    isUndoEnable = true,
                    isRedoEnable = false
                )
            }
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

            DrawingTool.DOUBLE_ARROW -> {
                createDoubleArrowPath(start = startOffset, end = offset)
            }

            DrawingTool.STAR ->{
                createStarPath(start = startOffset, end = offset)
            }
        }
        updatedWhiteBoardId.value?.let { id ->
            _state.update {
                it.copy(
                    currentPath = DrawnPath(
                        path = updatedPath,
                        drawnPath = state.value.selectedDrawingTool,
                        strokeWidth = state.value.strokeWidth,
                        backgroundColor = state.value.backgroundColor,
                        opacity = state.value.opacity,
                        strokeColor = state.value.strokeColor, whiteBoardId = id
                    )
                )
            }
        }
    }

    private fun createStarPath(
        start: Offset,
        end: Offset, numPoints : Int = 5, cornerRadius: Float = 12f
    ): Path {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)

        val width = right - left
        val height = bottom - top
        val center = Offset(left + width / 2, top + height / 2)

        val outerRadius = min(width, height) / 2
        val innerRadius = outerRadius / 2.5f

        val path = Path()
        val angleStep = (Math.PI / numPoints).toFloat()

        val points = mutableListOf<Offset>()

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * angleStep - Math.PI / 2
            val x = center.x + (cos(angle) * r).toFloat()
            val y = center.y + (sin(angle) * r).toFloat()
            points.add(Offset(x, y))
        }

        for (i in points.indices) {
            val current = points[i]
            val prev = points[(i - 1 + points.size) % points.size]
            val next = points[(i + 1) % points.size]

            val v1 = (current - prev).normalize()
            val v2 = (current - next).normalize()

            val p1 = current - v1 * cornerRadius
            val p2 = current - v2 * cornerRadius

            if (i == 0) {
                path.moveTo(p1.x, p1.y)
            } else {
                path.lineTo(p1.x, p1.y)
            }

            path.quadraticTo(current.x, current.y, p2.x, p2.y)
        }

        path.close()
        return path
    }

    private fun createEraserPath(continuingOffset: Offset): Path {
        return Path().apply {
            addOval(Rect(center = continuingOffset, radius = 10f))
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

    private fun createDoubleArrowPath(start: Offset, end: Offset): Path {
        val arrowHeadLength = 40f
        val arrowHeadAngle = 40f
        val angle = atan2(end.y - start.y, end.x - start.x)
        val angleRad = Math.toRadians(arrowHeadAngle.toDouble()).toFloat()
        val startAngle = angle + Math.PI.toFloat()
        val startArrow1 = Offset(
            x = start.x - arrowHeadLength * cos(startAngle - angleRad),
            y = start.y - arrowHeadLength * sin(startAngle - angleRad)
        )
        val startArrow2 = Offset(
            x = start.x - arrowHeadLength * cos(startAngle + angleRad),
            y = start.y - arrowHeadLength * sin(startAngle + angleRad)
        )

        val endArrow1 = Offset(
            x = end.x - arrowHeadLength * cos(angle - angleRad),
            y = end.y - arrowHeadLength * sin(angle - angleRad)
        )
        val endArrow2 = Offset(
            x = end.x - arrowHeadLength * cos(angle + angleRad),
            y = end.y - arrowHeadLength * sin(angle + angleRad)
        )

        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)

            moveTo(start.x, start.y)
            lineTo(startArrow1.x, startArrow1.y)
            moveTo(start.x, start.y)
            lineTo(startArrow2.x, startArrow2.y)

            moveTo(end.x, end.y)
            lineTo(endArrow1.x, endArrow1.y)
            moveTo(end.x, end.y)
            lineTo(endArrow2.x, endArrow2.y)
        }
    }

    private fun createArrowPath(start: Offset, end: Offset): Path {
        val arrowHeadLength = 40f
        val arrowHeadAngle = 40f

        val angle = atan2(end.y - start.y, end.x - start.x)
        val angleRad = Math.toRadians(arrowHeadAngle.toDouble()).toFloat()
        val endArrow1 = Offset(
            x = end.x - arrowHeadLength * cos(angle - angleRad),
            y = end.y - arrowHeadLength * sin(angle - angleRad)
        )
        val endArrow2 = Offset(
            x = end.x - arrowHeadLength * cos(angle + angleRad),
            y = end.y - arrowHeadLength * sin(angle + angleRad)
        )

        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)

            moveTo(end.x, end.y)
            lineTo(endArrow1.x, endArrow1.y)
            moveTo(end.x, end.y)
            lineTo(endArrow2.x, endArrow2.y)
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
                _state.update { state ->
                    val undoStack = if (state.undoStack.isEmpty() && state.redoStack.isEmpty()) {
                        ArrayDeque(paths)
                    } else {
                        state.undoStack
                    }
                    state.copy(
                        paths = paths,
                        undoStack = undoStack,
                        isUndoEnable = undoStack.isNotEmpty(),
                        isRedoEnable = state.redoStack.isNotEmpty()
                    )
                }
            }
        }
    }

    private fun Offset.normalize(): Offset {
        val length = getDistance()
        return if (length != 0f) Offset(x = x / length, y / length) else Offset.Zero
    }
}