(ns jiksnu.viewmodel
  (:use [jiksnu.model :only [_model _view class-names model-names]])
  (:require [lolg :as log]
            [jiksnu.logging :as jl]
            [jiksnu.model :as model]))

(def *logger* (log/get-logger "jiksnu.viewmodel"))

(defn update-pages
  [data]
  (log/finest *logger* "Updating pages")
  (when-let [pages (.-pages data)]
    (doseq [pair (js->clj pages)]
      (let [[k v] pair]
        (log/fine *logger* (format "adding page: %s" k))
        (let [page (clj->js (assoc v :id k))]
          (.add model/pages (jl/spy page)
                (js-obj "at" k)))))))

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

(defn update-title
  "set the title of the page"
  [data]
  (when-let [title (.-title data)]
    (.set model/_model "title" title)))

(defn update-currents
  [data]
  (when-let [currentUser (.-currentUser data)]
    (.set model/_model "currentUser" currentUser)))

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

