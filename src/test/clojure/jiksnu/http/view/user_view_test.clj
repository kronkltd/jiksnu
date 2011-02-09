(ns jiksnu.http.view.user-view-test
  (:use jiksnu.http.view.user-view
        [lazytest.describe :only (describe it testing given)]
        jiksnu.http.view
        jiksnu.view
        ciste.core))

(describe uri "User")

(describe title "User")

(describe avatar-img)

(describe display-minimal "User")

(describe index-table-line "User")

(describe add-form "User")

(describe edit-form "User")

(describe subscribe-form)

(describe unsubscribe-form)

(describe user-actions)

(describe show-section "User")

(describe apply-view "#'index :html")

(describe apply-view "#'show :html")

(describe apply-view "#'edit :html")

(describe apply-view "#'update :html")

(describe apply-view "#'delete :html")

