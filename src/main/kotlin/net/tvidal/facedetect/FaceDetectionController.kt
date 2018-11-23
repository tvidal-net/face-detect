package net.tvidal.facedetect

import net.tvidal.facedetect.CascadeClassifierWrapper.Companion.MAX_SIZE
import javafx.application.Platform.runLater
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.videoio.VideoCapture
import java.util.Collections.singleton
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

open class FaceDetectionController {

    @FXML
    private lateinit var originalFrame: ImageView

    private val capture = VideoCapture()

    private val faceClassifier = CascadeClassifierWrapper("frontalface_alt", 1.05, 6, MAX_SIZE, Size())
    private val eyesClassifier = CascadeClassifierWrapper("eye_tree_eyeglasses", 1.1, 16)
    private val smileClassifier = CascadeClassifierWrapper("smile", 1.4, 32)

    private val timer: ScheduledExecutorService = newSingleThreadScheduledExecutor()

    companion object {
        private const val FRAMES_PER_SECOND = 16
        private val DELAY = SECONDS.toMillis(1) / FRAMES_PER_SECOND

        private val FACE_COLOR = Scalar(0.0, 0.0, 255.0)
        private val EYE_COLOR = Scalar(255.0, 255.0, 0.0)
        private val SMILE_COLOR = Scalar(0.0, 255.0, 0.0)
    }

    fun init() {
        runLater { startCamera() }
    }

    private fun startCamera() {
        if (!capture.isOpened) {
            capture.open(0)
            if (capture.isOpened) {
                val frameGrabber = Runnable {
                    val frame = grabFrame()
                    val imageToShow = mat2Image(frame)
                    onFXThread(originalFrame.imageProperty(), imageToShow)
                }
                timer.scheduleAtFixedRate(frameGrabber, 0, DELAY, MILLISECONDS)

            } else {
                System.err.println("Failed to open the camera connection...")
            }

        } else {
            stopAcquisition()
        }
    }

    private fun grabFrame(): Mat {
        val frame = Mat()

        if (capture.isOpened) {
            try {
                capture.read(frame)
                if (!frame.empty()) detectAndDisplay(frame)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return frame
    }

    private fun detectAndDisplay(frame: Mat) {

        val grayFrame = frame.toGrayScale()

        val faces = faceClassifier.detectObjects(grayFrame)
        faces.forEach { face ->

            val topHalf = face.topHalf()
            val eyes = eyesClassifier.detectObjects(grayFrame, topHalf)
            frame.submat(topHalf).draw(eyes, EYE_COLOR)

            val bottomHalf = face.bottomHalf()
            val smiles = smileClassifier.detectObjects(grayFrame, bottomHalf)

            val smiling = !smiles.isEmpty()

            val color = if (smiling) SMILE_COLOR else FACE_COLOR
            frame.draw(singleton(face), color, 3)

            val text = if (smiling) "happy" else "angry"
            frame.subText(text, face, color)
        }
    }

    fun stopAcquisition() {
        if (!timer.isShutdown) {
            try {
                timer.shutdown()
                timer.awaitTermination(DELAY, MILLISECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

}
