package com.muhammad.zigzag.presentation.screens.whiteboard

import android.annotation.SuppressLint
import android.graphics.RectF
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
import com.muhammad.zigzag.domain.model.PathStyle
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
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
                viewModelScope.launch {
                    upsertWhiteBoard()

                }
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
                upsertWhiteBoardPreview()
            }

            is WhiteboardEvent.OnDrawingToolClose -> {
                _state.update {
                    it.copy(isDrawingToolsCardVisible = false)
                }
            }

            is WhiteboardEvent.OnDrawingToolSelected -> {
                val newStyle = when (event.drawingTool) {
                    DrawingTool.RECTANGLE, DrawingTool.CIRCLE, DrawingTool.TRIANGLE,
                    DrawingTool.HEART, DrawingTool.STAR, DrawingTool.CLOUD -> PathStyle.Fill
                    DrawingTool.DASHED_LINE -> PathStyle.DASHED
                    DrawingTool.DOTTED_LINE -> PathStyle.DOTTED
                    DrawingTool.HIGHLIGHTER -> PathStyle.Highlighter
                    DrawingTool.SPRAY_PAINT -> PathStyle.Spray
                    else -> PathStyle.Stroke
                }

                _state.update { state ->
                    state.copy(
                        selectedDrawingTool = event.drawingTool,
                        backgroundColor = if (newStyle == PathStyle.Fill) state.backgroundColor else Color.Transparent
                    )
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
                val tool = state.value.selectedDrawingTool
                val style = when (tool) {
                    DrawingTool.RECTANGLE, DrawingTool.CIRCLE, DrawingTool.TRIANGLE,
                    DrawingTool.HEART, DrawingTool.STAR, DrawingTool.CLOUD, -> PathStyle.Fill
                    DrawingTool.DASHED_LINE -> PathStyle.DASHED
                    DrawingTool.DOTTED_LINE -> PathStyle.DOTTED
                    DrawingTool.HIGHLIGHTER -> PathStyle.Highlighter
                    DrawingTool.SPRAY_PAINT -> PathStyle.Spray
                    else -> PathStyle.Stroke
                }
                if (isFirstPath) {
                    viewModelScope.launch {
                        val newId = upsertWhiteBoard()
                        isFirstPath = false

                        addNewPath(event.offset, tool, style, newId)
                    }
                } else {
                    addNewPath(event.offset, tool, style, updatedWhiteBoardId.value ?: 0L)
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
                        viewModelScope.launch {
                            upsertWhiteBoard()
                        }
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
    private fun addNewPath(offset: Offset, tool: DrawingTool, style: PathStyle, boardId: Long) {
        _state.update { state ->
            state.copy(
                startingOffset = offset,
                currentPath = DrawnPath(
                    id = System.currentTimeMillis(),
                    path = Path().apply { moveTo(offset.x, offset.y) },
                    drawnPath = tool,
                    style = style,
                    strokeWidth = state.strokeWidth,
                    strokeColor = state.strokeColor,
                    backgroundColor = state.backgroundColor,
                    opacity = state.opacity,
                    whiteBoardId = boardId
                )
            )
        }
    }


    private fun onUndoPath() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState.undoStack.isNotEmpty()) {
                val updatedUndo = ArrayDeque(currentState.undoStack)
                val lastAction = updatedUndo.removeLast()
                val updatedRedo = ArrayDeque(currentState.redoStack).apply { addLast(lastAction) }
                when (lastAction) {
                    is PathAction.Add -> {
                        pathRepository.deletePath(lastAction.path)
                    }

                    is PathAction.Delete -> {
                        lastAction.paths.forEach { path -> pathRepository.upsertPath(path) }
                    }
                }
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
                val lastAction = updatedRedo.removeLast()
                val updatedUndo = ArrayDeque(currentState.undoStack).apply { addLast(lastAction) }
                when (lastAction) {
                    is PathAction.Add -> {
                        pathRepository.upsertPath(lastAction.path)
                    }

                    is PathAction.Delete -> {
                        lastAction.paths.forEach { path -> pathRepository.deletePath(path) }
                    }
                }
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
    private suspend fun upsertWhiteBoard() : Long {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val updatedWhiteBoard = WhiteBoard(
                name = state.value.whiteBoardName,
                lastEdited = today,
                canvasColor = state.value.canvasColor,
                id = updatedWhiteBoardId.value
            )
            val newId = whiteBoardRepository.upsertWhiteboard(updatedWhiteBoard)
            updatedWhiteBoardId.value = newId
        return newId
    }

    private fun deletePaths(paths: List<DrawnPath>) {
        viewModelScope.launch {
            paths.forEach { path -> pathRepository.deletePath(path) }
        }
    }

    private fun insertPath(path: DrawnPath) {
        viewModelScope.launch {
            println("Insert path : $path")
            pathRepository.upsertPath(path)
            println("Inserted path : $path")
            _state.update {
                it.copy(
                    undoStack = ArrayDeque(it.undoStack).apply { addLast(PathAction.Add(path)) },
                    redoStack = ArrayDeque(),
                    isUndoEnable = true,
                    isRedoEnable = false
                )
            }
        }
    }


    private fun updateContinueOffsets(offset: Offset) {
        val startOffset =state.value.startingOffset
        val updatedPath = when (state.value.selectedDrawingTool) {
            DrawingTool.PEN, DrawingTool.LASER_PEN -> createFreeHandPath(startOffset, offset)
            DrawingTool.HIGHLIGHTER -> createHighlighterPath(startOffset, offset)
            DrawingTool.ERASER -> {
                updatePathsToBeDeleted(startOffset, offset)
                createEraserPath(offset)
            }

            DrawingTool.LINE, DrawingTool.DOTTED_LINE, DrawingTool.DASHED_LINE -> createLinePath(startOffset, offset)
            DrawingTool.ARROW -> createArrowPath(startOffset, offset)
            DrawingTool.RECTANGLE -> createRectanglePath(startOffset, offset)
            DrawingTool.CIRCLE -> createCirclePath(startOffset, offset)
            DrawingTool.TRIANGLE -> createTrianglePath(startOffset, offset)
            DrawingTool.DOUBLE_ARROW -> createDoubleArrowPath(startOffset, offset)
            DrawingTool.STAR -> createStarPath(startOffset, offset)
            DrawingTool.HEART -> createHeartPath(startOffset, offset)
            DrawingTool.CLOUD -> createCloudPath(startOffset, offset)
            DrawingTool.SPRAY_PAINT -> createSprayPaintPath(startOffset, offset)
        }

        _state.update { state ->
            state.copy(
                currentPath = state.currentPath?.copy(
                    path = updatedPath
                )
            )
        }
    }

    private fun createStarPath(
        start: Offset,
        end: Offset, numPoints: Int = 5, cornerRadius: Float = 12f,
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

    private fun createHeartPath(start: Offset, end: Offset, roundness: Float = 0.35f): Path {
        val left = minOf(start.x, end.x)
        val right = maxOf(start.x, end.x)
        val top = minOf(start.y, end.y)
        val bottom = maxOf(start.y, end.y)

        val width = right - left
        val height = bottom - top

        val size = max(1f, minOf(width, height))

        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f

        val topY = cy - size * 0.28f
        val bottomY = cy + size * 0.40f

        val cpTopY = cy - size * (0.7f - 0.2f * roundness)
        val cpRightX = cx + size * (0.5f + 0.15f * roundness)
        val cpLeftX = cx - size * (0.5f + 0.15f * roundness)

        val cpInnerY = cy - size * (0.05f + 0.05f * roundness)

        return Path().apply {
            reset()
            moveTo(cx, topY)

            cubicTo(
                cpRightX, cpTopY,
                cx + size * 0.95f, cpInnerY,
                cx, bottomY
            )

            cubicTo(
                cx - size * 0.95f, cpInnerY,
                cpLeftX, cpTopY,
                cx, topY
            )

            close()
        }
    }

    private fun createCloudPath(start: Offset, end: Offset): Path {
        val left = min(start.x, end.x)
        val top = min(start.y, end.y)
        val right = max(start.x, end.x)
        val bottom = max(start.y, end.y)
        val width = right - left
        val height = bottom - top

        return Path().apply {
            moveTo(left + width * 0.2f, bottom)

            cubicTo(
                left, bottom,
                left, top + height * 0.5f,
                left + width * 0.25f, top + height * 0.5f
            )

            cubicTo(
                left + width * 0.2f, top,
                left + width * 0.5f, top,
                left + width * 0.5f, top + height * 0.25f
            )

            cubicTo(
                left + width * 0.6f, top,
                right - width * 0.2f, top,
                right - width * 0.2f, top + height * 0.3f
            )

            cubicTo(
                right, top + height * 0.5f,
                right, bottom,
                left + width * 0.8f, bottom
            )

            close()
        }
    }


    private fun createFreeHandPath(start: Offset, end: Offset): Path {
        val existingPath = state.value.currentPath?.path
        return Path().apply {
            if (existingPath != null) {
                addPath(existingPath)
                lineTo(end.x, end.y)
            } else {
                moveTo(start.x, start.y)
                lineTo(end.x, end.y)
            }
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

    private fun createSprayPaintPath(
        start: Offset,
        end: Offset,
        density: Int = 200,
        radius: Float = 12f,
    ): Path {
        val path = Path()
        val dx = end.x - start.x
        val dy = end.y - start.y

        for (i in 0..density) {
            val t = i / density.toFloat()
            val x = start.x + dx * t
            val y = start.y + dy * t

            val randAngle = Random.nextFloat() * (2 * Math.PI).toFloat()
            val randRadius = Random.nextFloat() * radius
            val scatterX = x + cos(randAngle) * randRadius
            val scatterY = y + sin(randAngle) * randRadius

            path.addOval(
                RectF(
                    scatterX - 1.5f,
                    scatterY - 1.5f,
                    scatterX + 1.5f,
                    scatterY + 1.5f
                ).toComposeRect()
            )
        }

        return path
    }

    private fun createHighlighterPath(
        start: Offset,
        end: Offset,
        thickness: Float = 40f,
    ): Path {
        val path = Path()
        val dx = end.x - start.x
        val dy = end.y - start.y
        val angle = atan2(dy, dx)

        val px = -sin(angle) * (thickness / 2f)
        val py = cos(angle) * (thickness / 2f)

        val p1 = Offset(start.x + px, start.y + py)
        val p2 = Offset(end.x + px, end.y + py)
        val p3 = Offset(end.x - px, end.y - py)
        val p4 = Offset(start.x - px, start.y - py)

        path.moveTo(p1.x, p1.y)
        path.lineTo(p2.x, p2.y)
        path.lineTo(p3.x, p3.y)
        path.lineTo(p4.x, p4.y)
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
                    val actions = ArrayDeque<PathAction>().apply {
                        paths.forEach { path -> addLast(PathAction.Add(path)) }
                    }
                    state.copy(
                        paths = paths,
                        undoStack = actions,
                        isUndoEnable = state.undoStack.isNotEmpty(),
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

    fun actionsToAddedPaths(stack: ArrayDeque<PathAction>): List<DrawnPath> {
        return stack.toList().mapNotNull { (it as? PathAction.Add)?.path }
    }

    fun RectF.toComposeRect(): Rect = Rect(left, top, right, bottom)
}