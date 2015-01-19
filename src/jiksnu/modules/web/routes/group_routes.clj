(ns jiksnu.modules.web.routes.group-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.modules.web.routes :as r]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Group))

(defgroup groups
  :url "/main/groups"
  :name "groups"

  )

(defresource groups collection
  :mixins [mixin/collection-resource]
  :available-media-types ["application/json"
                          "text/html"
                          ]
  ;; :count (fn [ctx]
  ;;          (log/info "counting activities")
  ;;          2)
  ;; :data-key :page
  :exists? (fn [ctx]
             {:page (log/spy :info (group/index))})
  :handle-ok (fn [ctx]
               (condp = (get-in ctx [:representation :media-type])
                 "text/html"        (helpers/index (:request ctx))
                 "application/json" (json/json-str (:page ctx)))))

;; (defresource groups resource

;;   )

(defn routes
  []
  [
   [[:get  "/api/statusnet/app/memberships/:id.:format"] #'group/user-list]

   [[:get  "/main/groups.:format"]                       #'group/index]
   ;; [[:get  "/main/groups"]                               #'group/index]
   [[:post "/main/groups"]                               #'group/create]
   [[:get  "/main/groups/new"]                           #'group/new-page]
   [[:get  "/main/groups/:name.:format"]                 #'group/show]
   ;; [[:get  "/main/groups/:name"]                         #'group/show]
   ;; [[:get  "/main/groups/:name/edit"]                    #'group/edit-page]
   [[:post "/main/groups/:name/join"]                    #'group/join]

   [[:get  "/model/groups/:id.:format"]                  #'group/show]
   ;; [[:get  "/model/groups/:id"]                          #'group/show]

   [[:get  "/users/:id/groups.:format"]                  #'group/fetch-by-user]
   ;; [[:get  "/users/:id/groups"]                          #'group/fetch-by-user]
   ;; [[:get    "/search/group"]                            #'group/search-page]
   ;; [[:post   "/search/group"]                            #'group/search]

   ])

(defn pages
  []
  [
   [{:name "groups"}    {:action #'group/index}]
   ])

(defn sub-pages
  []
  [
   [{:type Group :name "admins"}        {:action #'group/fetch-admins}]
   [{:type Group :name "conversations"} {:action #'conversation/fetch-by-group}]
  ])
