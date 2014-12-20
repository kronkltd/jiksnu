(ns jiksnu.modules.web.routes.template-routes
  (:require [jiksnu.modules.web.actions.template-actions :as templates]))

(defn routes
  []
  [
   [[:get "/partials/index-activities.html"]         #'templates/index-activities]
   [[:get "/partials/index-conversations.html"]      #'templates/index-conversations]
   [[:get "/partials/index-domains.html"]            #'templates/index-domains]
   ;; [[:get "/partials/index-feeds.html"]              #'templates/index-feeds]
   [[:get "/partials/index-feed-sources.html"]       #'templates/index-feed-sources]
   [[:get "/partials/index-feed-subscriptions.html"] #'templates/index-feed-subscriptions]
   [[:get "/partials/index-groups.html"]             #'templates/index-groups]
   [[:get "/partials/index-group-members.html"]      #'templates/index-group-members]
   [[:get "/partials/index-resources.html"]          #'templates/index-resources]
   [[:get "/partials/index-users.html"]              #'templates/index-users]

   [[:get "/partials/left-nav.html"]                 #'templates/left-nav]

   [[:get "/partials/public-timeline.html"]          #'templates/public-timeline]
   [[:get "/partials/show-activity.html"]            #'templates/show-activity]
   [[:get "/partials/show-domain.html"]              #'templates/show-domain]
   [[:get "/partials/show-user.html"]                #'templates/show-user]
   ])
