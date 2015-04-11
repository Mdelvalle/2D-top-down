(ns zombies.core.desktop-launcher
  (:require [zombies.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. zombies "zombies" 800 600)
  (Keyboard/enableRepeatEvents true))
