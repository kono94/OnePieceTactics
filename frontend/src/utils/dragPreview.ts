const DEFAULT_DRAG_PREVIEW_SIZE = 64

export function setUnitDragPreview(evt: DragEvent, imageSrc: string | null | undefined, size = DEFAULT_DRAG_PREVIEW_SIZE): (() => void) | null {
    if (!evt.dataTransfer || typeof document === 'undefined') return null

    const preview = document.createElement('div')
    preview.style.position = 'fixed'
    preview.style.left = '-1000px'
    preview.style.top = '-1000px'
    preview.style.width = `${size}px`
    preview.style.height = `${size}px`
    preview.style.overflow = 'hidden'
    preview.style.borderRadius = '8px'
    preview.style.background = '#0f172a'
    preview.style.border = '2px solid rgba(255, 255, 255, 0.7)'
    preview.style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.35)'
    preview.style.pointerEvents = 'none'
    preview.style.boxSizing = 'border-box'

    if (imageSrc) {
        const img = document.createElement('img')
        img.src = imageSrc
        img.draggable = false
        img.style.display = 'block'
        img.style.width = '100%'
        img.style.height = '100%'
        img.style.objectFit = 'cover'
        preview.appendChild(img)
    }

    document.body.appendChild(preview)
    evt.dataTransfer.setDragImage(preview, size / 2, size / 2)

    let cleanedUp = false
    return () => {
        if (cleanedUp) return
        cleanedUp = true
        preview.remove()
    }
}
