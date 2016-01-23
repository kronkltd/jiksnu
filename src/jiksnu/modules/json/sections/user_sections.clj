(ns jiksnu.modules.json.sections.user-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [title uri show-section edit-button
                                            index-line show-section-minimal
                                            update-button index-block
                                            index-section]]
            [jiksnu.modules.core.sections :refer [admin-index-block
                                                  admin-index-line
                                                  admin-index-section
                                                  admin-show-section]]
            [jiksnu.modules.web.sections :refer [dropdown-menu]]
            [jiksnu.session :refer [current-user is-admin?]]
            [slingshot.slingshot :refer [try+]])
  (:import jiksnu.model.User))

(defsection show-section [User :twitter]
  [item & _]
  {:name (:name item)
   :id (:_id item)
   :screen_name (:username item)
   :url (:id item)
   :profile_image_url (:avatarUrl item)
   :protected false})

(defsection show-section [User :json]
  [item & _]
  (with-format :model (show-section item)))
