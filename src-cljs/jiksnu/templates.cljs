(ns jiksnu.templates
  (:require [hipo :as hipo :include-macros true]))

(defn config [& _]
  (.warn js/console "config called"))

(defn control-line
  [label name type & {:as options}]
  (let [{:keys [value checked]} options]
    [:div.control-group
     [:label.control-label {:for name} label]
     [:div.controls
      [:input
       (merge {:type type :name name}
              (when value
                {:value value})
              (when checked
                {:checked "checked"}))]]]))

