(ns zombies.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]))

(declare zombies main-screen)
(def ^:const duration 0.3)
(def ^:const damping 0.9)
(def ^:const max-velocity 100)
(def ^:const deceleration 0.3)

(defn decelerate
  [velocity]
  (let [vel (* velocity deceleration)]
    (if (< (Math/abs velocity) damping)
      0
      vel)))

(defn x-velocity
  [{:keys [me? x-vel]}]
  (if me?
    (cond
      (key-pressed? :dpad-left) (* -1 max-velocity)
      (key-pressed? :dpad-right) max-velocity
      :else x-vel)
    x-vel))

(defn y-velocity
  [{:keys [me? y-vel]}]
  (if me?
    (cond
      (key-pressed? :dpad-up) max-velocity 
      (key-pressed? :dpad-down) (* -1 max-velocity)
      :else y-vel)
    y-vel))

(defn get-direction
  [{:keys [x-vel y-vel dir]}]
  (cond
    (> x-vel 0) :right
    (< x-vel 0) :left
    (> y-vel 0) :up
    (< y-vel 0) :down
    :else dir))

(defn create
  "Attributes that the main entity starts with."
  ;[stand-r moving-r moving-r-2 stand-u moving-u moving-u2]
  [sta-r mov-r mov-r2 sta-u mov-u mov-u2]
  (assoc sta-r
         :stand-right sta-r
         :stand-left  (texture sta-r :flip true false)
         :stand-up    sta-u
         :stand-down  (texture sta-u :flip false true)
         :walk-right  (animation duration
                                 [mov-r mov-r2]
                                 :set-play-mode (play-mode :loop-pingpong))
         :walk-left   (animation duration
                                 (map #(texture % :flip true false) [mov-r mov-r2])
                                 :set-play-mode (play-mode :loop-pingpong))
         :walk-up     (animation duration
                                 [mov-u mov-u2]
                                 :set-play-mode (play-mode :loop-pingpong))
         :walk-down   (animation duration
                                 (map #(texture % :flip false true) [mov-u mov-u2])
                                 :set-play-mode (play-mode :loop-pingpong))
         :x-vel       0
         :y-vel       0
         :x           300
         :y           300
         :width       32
         :height      32
         :me?         true
         :direction   :right))

(defn move
  [{:keys [delta-time]} {:keys [x y] :as entity}]
  (let [x-vel    (x-velocity entity)
        y-vel    (y-velocity entity)
        x-change (* x-vel delta-time)
        y-change (* y-vel delta-time)]
    ;; If char is moving
    (if (or (not= 0 x-change) (not= 0 y-change))
      (assoc entity
             :x-vel    (decelerate x-vel)
             :y-vel    (decelerate y-vel)
             :x-change x-change
             :y-change y-change
             :x        (+ x x-change)
             :y        (+ y y-change))
      entity)))

(defn animate
  [screen {:keys [x-vel       y-vel
                  stand-right stand-left
                  stand-up    stand-down
                  walk-right  walk-left
                  walk-up     walk-down] :as entity}]
  (let [direction (get-direction entity)]
    (merge entity
           (cond
             (not= y-vel 0)
               (if (= direction :up)
                 (animation->texture screen walk-up)
                 (animation->texture screen walk-down))
             (not= x-vel 0)
               (if (= direction :right)
                 (animation->texture screen walk-right)
                 (animation->texture screen walk-left))
             :else
               (cond
                 (= direction :right) stand-right
                 (= direction :left)  stand-left
                 (= direction :down)  stand-down
                 (= direction :up)    stand-up))
           {:direction direction})))

(defn create-tile
  [tile-type img x y]
  (assoc img
         :tile-type tile-type
         :position  [x y]
         

(defn create-map
  []
  (let [tile (texture "white_tile.png")]
    (assoc (create-tile tile))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer)
    (let [sta-r  (texture "guy-S-R.png")
          mov-r  (texture "guy-M-R.png")
          mov-r2 (texture "guy-M-R2.png")
          sta-u  (texture "guy-S-U.png")
          mov-u  (texture "guy-M-U.png")
          mov-u2 (texture "guy-M-U2.png")
          white-t  (assoc (texture "white_tile.png") :tile? true :x 320 :y 320)
          wall  (texture "black_wall.png")]
      (apply create [sta-r mov-r mov-r2 sta-u mov-u mov-u2])))

  :on-render
  (fn [screen entities]
    (clear!)
    (->> entities
      (map #(->> %
              (move screen)
              (animate screen)))
      (render! screen)))

  :on-key-down
  (fn [screen entities]
    (cond
      ;(is-pressed? :r) (app! :post-runnable (#set-screen! zombies main-screen))
      :else entities)))

(defgame zombies
  :on-create
  (fn [this]
    (set-screen! this main-screen)))

(-> main-screen :entities deref)
