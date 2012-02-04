(ns jiksnu.sections.subscription-sections)

(defn subscriber-line
  [subscription]
  [:li
   (sections.user/display-avatar
    (get-to subscription) 24)])

(defn subscribers-section
  [user subscribers]
  (when user
    [:div.subscribers
     [:h3
      ;; subscribers link
      [:a {:href (str "/" (:username user) "/subscribers")}]]
     [:ul.unstyled
      [:li (map subscriber-line subscribers)]]]))
