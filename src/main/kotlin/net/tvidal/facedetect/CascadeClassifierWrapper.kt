package net.tvidal.facedetect

import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.objdetect.Objdetect.CASCADE_FIND_BIGGEST_OBJECT
import org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE

class CascadeClassifierWrapper(
    classifierName: String,
    private val scaleFactor: Double,
    private val minNeighbours: Int,
    private val minSize: Size = MIN_SIZE,
    private val maxSize: Size = MAX_SIZE
) {

    companion object {
        val MIN_SIZE = Size(24.0, 12.0)
        val MAX_SIZE = Size(144.0, 144.0)
        const val MULTISCALE_FLAGS = CASCADE_SCALE_IMAGE or CASCADE_FIND_BIGGEST_OBJECT
    }

    private val cascadeClassifier = createCascadeClassifier(classifierName)

    fun detectObjects(fullFrame: Mat, area: Rect? = null): List<Rect> {

        val frame = if (area == null) fullFrame else fullFrame.submat(area)

        val objects = MatOfRect()
        cascadeClassifier.detectMultiScale(
            frame,
            objects,
            scaleFactor,
            minNeighbours,
            MULTISCALE_FLAGS,
            minSize,
            maxSize
        )
        return objects.toList()
    }
}
