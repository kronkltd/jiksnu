(ns jiksnu.sections.worker-sections
  (:require [ciste.workers :as workers]))

(defn stop-workers-button
  []
  [:div
   [:form {:action "/admin/workers/all/stop"
           :method "POST"}
    [:button.btn.btn-danger {:type "submit"}
     "Stop All Workers"]]])

(defn worker-button
  [worker-key]
  [:form {:action "/admin/workers/start" :method "POST"}
   [:input {:type "hidden" :name "name" :value (name worker-key)}]
   [:input.btn {:type "SUBMIT" :value (name worker-key)}]])

(defn start-worker-form
  []
  [:form.well {:method "post" :action "/admin/workers/start"}
   [:fieldset
    [:legend "Start Worker"]
    [:div.control-group
     [:label {:for "name"} "Name"]
     [:div.input
      [:select {:name "name"}
       (map
        (fn [worker]
          [:option {:value worker} worker])
        (workers/worker-keys))]]]
      [:div.control-group
       [:label {:for "host"} "Host"]
       [:div.input
        [:select {:name "host"}
         (map
          (fn [host]
            [:option {:name "dev"} "dev"])
         [] #_(core.host/fetch-all))]]]
    [:div.actions
     [:input.btn.primary {:type "submit"}]]]])



(defn stop-button
  [id]
  [:form {:method "post" :action (str "/admin/workers/stop")}
   [:input {:type "hidden" :name "id" :value id}]
   [:button.btn.btn-danger {:type "submit"}
    [:i.icon-stop] [:span.button-text "Stop"]]])

(defn running-worker-section
  [workers]
  [:section
   [:h2 "Running Workers"]
   [:table.table
    [:tr
     [:th "Worker"]
     [:th "ID"]
     [:th "Stopping?"]
     [:th "Host"]
     [:th "Counter"]
     [:th "Stop"]]
    (map
     (fn [[n id stopping host counter]]
       [:tr
        [:td n]
        [:td id]
        [:td stopping]
        [:td host]
        [:td counter]
        [:th (stop-button id)]])
     workers)]
   (stop-workers-button)])

(defn available-worker-section
  [data]
  [:section
   [:h2 "Available Workers"]
   [:ul
    (map (fn [key]
           [:li (worker-button key)])
         (sort (workers/worker-keys)))]])

