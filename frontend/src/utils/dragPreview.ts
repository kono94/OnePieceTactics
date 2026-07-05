const DEFAULT_DRAG_PREVIEW_SIZE = 64
const PREVIEW_BORDER_WIDTH = 2

type DragPreviewSource = HTMLImageElement | string | null | undefined

export function setUnitDragPreview(evt: DragEvent, source: DragPreviewSource, size?: number): (() => void) | null {
    if (!evt.dataTransfer || typeof document === 'undefined') return null

    const cssSize = size ?? getPreviewSize(source)
    const pixelRatio = window.devicePixelRatio || 1
    const preview = document.createElement('canvas')
    preview.width = Math.round(cssSize * pixelRatio)
    preview.height = Math.round(cssSize * pixelRatio)
    preview.style.position = 'fixed'
    preview.style.left = '0'
    preview.style.top = '0'
    preview.style.width = `${cssSize}px`
    preview.style.height = `${cssSize}px`
    preview.style.pointerEvents = 'none'
    preview.style.zIndex = '-1'

    drawCircularPreview(preview, source, cssSize, pixelRatio)

    document.body.appendChild(preview)
    evt.dataTransfer.setDragImage(preview, cssSize / 2, cssSize / 2)

    let cleanedUp = false
    return () => {
        if (cleanedUp) return
        cleanedUp = true
        preview.remove()
    }
}

function getPreviewSize(source: DragPreviewSource) {
    if (!(source instanceof HTMLImageElement)) return DEFAULT_DRAG_PREVIEW_SIZE

    const rect = source.getBoundingClientRect()
    const renderedSize = Math.max(rect.width, rect.height)
    if (!Number.isFinite(renderedSize) || renderedSize <= 0) return DEFAULT_DRAG_PREVIEW_SIZE

    return Math.round(renderedSize + PREVIEW_BORDER_WIDTH * 2)
}

function drawCircularPreview(canvas: HTMLCanvasElement, source: DragPreviewSource, size: number, pixelRatio: number) {
    const context = canvas.getContext('2d')
    if (!context) return

    const borderWidth = PREVIEW_BORDER_WIDTH
    const radius = size / 2
    const image = source instanceof HTMLImageElement ? source : null

    context.scale(pixelRatio, pixelRatio)
    context.clearRect(0, 0, size, size)
    context.save()
    context.beginPath()
    context.arc(radius, radius, radius - borderWidth, 0, Math.PI * 2)
    context.clip()

    if (image && image.complete && image.naturalWidth > 0 && image.naturalHeight > 0) {
        drawCoverImage(context, image, borderWidth, borderWidth, size - borderWidth * 2, size - borderWidth * 2)
    } else {
        context.fillStyle = '#0f172a'
        context.fillRect(0, 0, size, size)
    }

    context.restore()
    context.beginPath()
    context.arc(radius, radius, radius - borderWidth, 0, Math.PI * 2)
    context.lineWidth = borderWidth
    context.strokeStyle = 'rgba(255, 255, 255, 0.85)'
    context.stroke()
}

function drawCoverImage(
    context: CanvasRenderingContext2D,
    image: HTMLImageElement,
    targetX: number,
    targetY: number,
    targetWidth: number,
    targetHeight: number,
) {
    const sourceRatio = image.naturalWidth / image.naturalHeight
    const targetRatio = targetWidth / targetHeight
    const sourceWidth = sourceRatio > targetRatio ? image.naturalHeight * targetRatio : image.naturalWidth
    const sourceHeight = sourceRatio > targetRatio ? image.naturalHeight : image.naturalWidth / targetRatio
    const sourceX = (image.naturalWidth - sourceWidth) / 2
    const sourceY = (image.naturalHeight - sourceHeight) / 2

    context.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, targetX, targetY, targetWidth, targetHeight)
}
