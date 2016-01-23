(ns jiksnu.modules.web.sections.activity-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section edit-button link-to uri title index-block
                                            index-line index-section update-button]]
            [jiksnu.modules.core.sections :refer [admin-index-line
                                                  admin-index-block
                                                  admin-index-section]]
            [jiksnu.modules.core.sections.user-sections :as sections.core.user]
            [jiksnu.modules.web.sections :refer [action-link bind-to
                                                 control-line dropdown-menu
                                                 format-links]]
            [jiksnu.modules.web.sections.user-sections :as sections.web.user]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn index-formats
  [activities]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:as        "/api/statuses/public_timeline.as"]
    [:atom      "/api/statuses/public_timeline.atom"]
    [:json      "/api/statuses/public_timeline.json"]
    [:n3        "/api/statuses/public_timeline.n3"]
    [:rdf       "/api/statuses/public_timeline.rdf"]
    [:xml       "/api/statuses/public_timeline.xml"]]))

(defn timeline-formats
  [user]
  (map
   (fn [[f h]]
     (let [def (format-links f)]
       (merge def
              {:href h})))
   [[:json (sections.core.user/user-timeline-link user "json")]
    [:atom (sections.core.user/user-timeline-link user "atom")]
    [:as   (sections.core.user/user-timeline-link user "as")]
    [:n3   (sections.core.user/user-timeline-link user "n3")]
    [:rdf  (sections.core.user/user-timeline-link user "rdf")]
    [:xml  (sections.core.user/user-timeline-link user "xml")]]))

(defn like-button
  [activity]
  (action-link "activity" "like" (:_id activity)
               {:title "Like"
                :icon "heart"}))

(declare posted-link-section)

(defn show-comment
  [activity]
  (let [author (User.)]
    [:div.comment {:data-model "activity"}
     [:p
      [:span {:data-bind "with: author"}
       [:span {:data-model "user"}
        (sections.web.user/display-avatar author)
        (link-to author)]]
      ": "
      [:span "{{activity.title}}"]]
     #_[:p (posted-link-section activity)]]))

;; move to model

(defn comment-button
  [activity]
  [:a {:href "#"}
   [:i.icon-comment]
   [:span.button-text "Comment"]])

(defn model-button
  [activity]
  [:a {:href "/model/activities/{{activity.id}}.model" }
   "Model"])

(defn links-section
  [activity]
  [:h3 "Links"])

(defn maps-section
  [activity]

  (let [geo {}]
    [:div.map-section
     (bind-to "geo"
              #_[:img.map
                 {:alt ""
                  :src
                  ;; TODO: use urly to construct this
                  ;; TODO: Move this to cljs
                  (str "https://maps.googleapis.com/maps/api/staticmap?"
                       (string/join "&amp;"
                                    ["size=200x200"
                                     "zoom=11"
                                     "sensor=true"
                                     (str "markers=color:red|"
                                          (:latitude geo)
                                          ","
                                          (:longitude geo))]))}]
              [:p "Latitude: " [:span {:data-bind "text: latitude"}
                                (:latitude geo)]]
              [:p "Longitude: " [:span {:data-bind "text: longitude"}
                                 (:longitude geo)]])]))

(defn likes-section
  [activity]
  (when-let [likes [{}]]
    [:section.likes {:ng-if "likeCount > 0"}
     [:span "Liked by"]
     [:ul
      (map
       (fn [like]
         [:li
          "Person"
          #_(link-to (model.like/get-actor like))])
       likes)]]))

(defn tags-section
  [activity]
  [:div.tags {:ng-if "activity.tags.length > 0"}
   [:span "Tags: "]
   [:ul.tags
    [:li {:ng-repeat "tag in activity.tags"}
     [:a {:rel "tag"
          :href "/tags/{{tag}}"}
      "{{tag}}"]]]])

(defn enclosures-section
  [activity]
  [:ul.unstyled
   [:li {:data-model "resource"
         :ng-repeat "resource in resources"}
    [:div {:ng-if "resource.properties"}
     [:div {:data-bind "if: properties()['og:type'] === 'video'"}
      [:div.video-embed
       [:iframe
        {:frameborder "0"
         :allowfullscreen "allowfullscreen"
         :data-bind "attr: {src: properties()['og:video']}"}]]]]
    [:a {:rel "lightbox"
         :href "{{resource.url}}"}
     [:img.enclosure
      {:alt ""
       :ng-src "{{resource.url}}"}]]]])

(def post-sections
  [#'enclosures-section
   ;; #'links-section
   #'likes-section
   #'maps-section
   #'tags-section
   #'posted-link-section])

(defsection index-block [Activity :html]
  [records & [options & _]]
  [:div.activities
   {:data-bind "foreach: items"}
   (map #(index-line % options) records)])
