(ns jiksnu.viewmodel
  (:use [jiksnu.model :only [_model _view class-names model-names]])
  (:require [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]))

(def *logger* (log/get-logger "jiksnu.viewmodel"))

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

  (let [app (.get model/_model "app")]
    (.set app "loaded" true)
    (.set app "currentUser" (.-currentUser data))
    )

  ;; (update-pages     data)
  (update-page-info data)
  (update-post-form data)
  (update-targets   data)

  (.markdown (js/$ "*[data-provide='markdown']")))

