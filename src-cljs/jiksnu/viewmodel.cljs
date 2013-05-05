(ns jiksnu.viewmodel
  (:use [jayq.util :only [clj->js]]
        [jiksnu.model :only [_model _view class-names model-names
                             receive-model]])
  (:require [lolg :as log]))

(def *logger* (log/get-logger "jiksnu.viewmodel"))

(defn update-pages
  [data]
  (when-let [pages (.-pages data)]
    (let [page-model (.get _model "pages")]
      (doseq [pair (js->clj pages)]
        (let [[k v] pair]
          (log/info *logger* k)
          (let [page (clj->js (assoc v :id k))]
           (.add page-model page)))))))

(defn update-items
  "Adds all the data to their respective models"
  [data]
  (doseq [key model-names]
    (when-let [items (aget data key)]
      (let [coll (.get _model key)]
        (doseq [item items]
          (.add coll item))))))

(defn update-targets
  "Sets all the target models"
  [data]
  (doseq [model-name class-names]
    (let [key (str "target" model-name)]
      (when-let [id (aget data key)]
        (.set _model key id)))))

(defn update-page-info
  "Set page info data"
  [data]
  (when-let [page-info (.-pageInfo data)]
    (let [p (.get _model "pageInfo")]
      (.set p page-info))))

(defn update-post-form
  "Sets visibility of post form"
  [data]
  (if-let [post-form (.-postForm data)]
    (if-let [visible (.-visible post-form)]
      (.visible (.postForm _view) visible))))

(defn update-title
  "set the title of the page"
  [data]
  (when-let [title (.-title data)]
    (.set _model "title" title)))

(defn update-currents
  [data]
  (when-let [currentUser (.-currentUser data)]
    (.set _model "currentUser" currentUser)))

(defn process-viewmodel
  "Callback handler when a viewmodel is loaded"
  [data]
  ;; (def _m data)
  (update-pages     data)
  (update-title     data)
  ;; (update-items     data)
  (update-currents  data)
  (update-page-info data)
  (update-post-form data)
  (update-targets   data))

