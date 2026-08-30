package iamrp.dev.launcher.gestures.gestures

import iamrp.dev.launcher.gestures.Gesture
import iamrp.dev.launcher.gestures.GestureController

class LaunchAssistantGesture(controller: GestureController) :
    Gesture(controller, controller.launcher.prefs.gestureLaunchAssistant)
