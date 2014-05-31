(ns jiksnu.viewmodel
  (:use [jiksnu.model :only [_model _view class-names model-names]])
  (:require [goog.string :as gstring]
            [goog.string.format :as gformat]
            [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]))

(def *logger* (log/get-logger "jiksnu.viewmodel"))

(defn update-pages
  [data]
  (log/finest *logger* "Updating pages")
  (when-let [pages (.-pages data)]
    (doseq [pair (js->clj pages)]
      (let [[k v] pair]
        (let [page (clj->js (assoc v :id k))]
          (if-let [m (.get model/pages k)]
            (do
              (log/fine *logger* (gstring/format "setting existing page: %s" k))
              (.set m page))
            (do
              (log/fine *logger* (gstring/format "adding page: %s" k))
              (.add model/pages page))))))))

(defn update-items
  "Adds all the data to their respective models"
  [data]
  (doseq [key model-names]
    (when-let [items (aget data key)]
      (let [coll (.get model/_model key)]
        (doseq [item items]
          (.add coll item))))))

(defn update-targets
  "Sets all the target models"
  [data]
  (doseq [model-name class-names]
    (let [key (str "target" model-name)]
      (when-let [id (aget data key)]
        (.set model/_model key id)))))

(defn update-page-info
  "Set page info data"
  [data]
  (when-let [page-info (.-pageInfo data)]
    (let [p (.get model/_model "pageInfo")]
      (.set p page-info))))

(defn update-post-form
  "Sets visibility of post form"
  [data]
  (if-let [post-form (.-postForm data)]
    (if-let [visible (.-visible post-form)]
      (.visible (.postForm _view) visible))))

(defn process-viewmodel
  "Callback handler when a viewmodel is loaded"
  [data]
  ;; (def _m data)
  (.set model/_model "title"       (.-title data))
  (.set model/_model "formats"     (.-formats data))
  (.set model/_model "currentUser" (.-currentUser data))
  (update-pages     data)
  (update-page-info data)
  (update-post-form data)
  (update-targets   data)

  (.set model/_model "loaded" true)
  (.addClass (js/$ "html") "bound")

  (.markdown (js/$ "*[data-provide='markdown']"))
  )

