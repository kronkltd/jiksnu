(ns jiksnu.modules.web.views.oauth-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.oauth-actions :only [oauthapps]]))

(defview #'oauthapps :html
  [request _]

  ;; a form for adding oauth apps
  )

