(ns jiksnu.http.view.activity-view-test
  (:use jiksnu.http.view.activity-view
        jiksnu.http.view
        jiksnu.view
        ciste.core
        [lazytest.describe :only (describe it testing given)]))

(describe uri "Activity")

(describe add-form "Activity")

(describe display-minimal)

(describe edit-form)

(describe index-list-line)

(describe index-list-block)

(describe apply-view "#'index :atom")
