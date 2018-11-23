package net.tvidal.facedetect

import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.opencv.core.Core.NATIVE_LIBRARY_NAME
import java.lang.System.loadLibrary

open class FaceDetectionApp : Application() {

    override fun start(primaryStage: Stage) {
        try {
            val faceDetection = javaClass.getResource("/FaceDetection.fxml")
            val stylesheet = javaClass.getResource("/application.css")

            val loader = FXMLLoader(faceDetection)
            val root = loader.load<BorderPane>()

            val mainScene = Scene(root, 850.0, 620.0).apply {
                stylesheets.add(stylesheet.toExternalForm())
            }

            primaryStage.apply {
                title = "Face and Mood Detector"
                scene = mainScene
                isResizable = false
                show()
            }

            val controller = loader.getController<FaceDetectionController>()
            controller.init()

            primaryStage.onCloseRequest = EventHandler { controller.stopAcquisition() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        @JvmStatic fun main(vararg args: String) {
            loadLibrary(NATIVE_LIBRARY_NAME)
            launch(FaceDetectionApp::class.java, *args)
        }
    }
}
