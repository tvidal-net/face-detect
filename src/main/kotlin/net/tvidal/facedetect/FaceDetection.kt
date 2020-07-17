@file:JvmName("FaceDetection")

package net.tvidal.facedetect

import javafx.application.Platform.runLater
import javafx.beans.property.ObjectProperty
import javafx.embed.swing.SwingFXUtils.toFXImage
import javafx.scene.image.Image
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc.*
import org.opencv.objdetect.CascadeClassifier
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import java.io.FileNotFoundException
import java.lang.System.arraycopy
import java.nio.file.Files.notExists
import java.nio.file.Paths

private val PATH_FIX = Regex("^/(\\w:)")

private const val CASCADE_CLASSIFIER_PATH = "/usr/share/opencv4/haarcascades/haarcascade_"
private const val CASCADE_CLASSIFIER_EXTENSION = ".xml"

private const val TEXT_FONT = FONT_HERSHEY_DUPLEX
private const val TEXT_SIZE = 1.2
private const val TEXT_MARGIN = 16.0
private const val TEXT_THICKNESS = 2

fun createCascadeClassifier(classifierName: String): CascadeClassifier {
    val filePath = Paths.get("$CASCADE_CLASSIFIER_PATH$classifierName$CASCADE_CLASSIFIER_EXTENSION")
    if (notExists(filePath)) {
        throw FileNotFoundException("$filePath")
    }
    return CascadeClassifier(filePath.toString())
}

fun getFileResource(resourcePath: String): String? {
    val resource = FaceDetectionApp::class.java.getResource(resourcePath)
    if (resource != null) {
        val filePath = resource.path
        return PATH_FIX.replaceFirst(filePath, "\$1")
    }
    return null
}

fun <T> onFXThread(property: ObjectProperty<T>, value: T?) = runLater { property.set(value) }

fun Mat.toGrayScale(): Mat {
    val grayFrame = Mat()
    cvtColor(this, grayFrame, COLOR_BGR2GRAY)
    equalizeHist(grayFrame, grayFrame)
    return grayFrame
}

fun Mat.draw(rects: Iterable<Rect>, color: Scalar, thickness: Int = 2) {
    rects.forEach { rectangle(this, it.tl(), it.br(), color, thickness) }
}

fun Mat.subText(text: String, rect: Rect, color: Scalar) {
    val textPos = Point(rect.x + TEXT_MARGIN, rect.y + rect.height - TEXT_MARGIN)
    putText(
            this, text, textPos,
            TEXT_FONT,
            TEXT_SIZE, color,
            TEXT_THICKNESS
    )
}

fun Rect.topHalf() = Rect(x, y, width, halfHeight())

fun Rect.bottomHalf() = Rect(x, y + halfHeight(), width, halfHeight())

private fun Rect.halfHeight() = height / 2

fun mat2Image(frame: Mat): Image? = try {
    val bufferedImage = matToBufferedImage(frame)
    toFXImage(bufferedImage, null)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

private fun matToBufferedImage(original: Mat): BufferedImage {
    val width = original.width()
    val height = original.height()
    val channels = original.channels()
    val size = width * height * channels

    val sourcePixels = ByteArray(size)
    original.get(0, 0, sourcePixels)

    val imageType = if (channels > 1) TYPE_3BYTE_BGR else TYPE_BYTE_GRAY
    val image = BufferedImage(width, height, imageType)

    val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
    arraycopy(sourcePixels, 0, targetPixels, 0, size)

    return image
}
