(jiksnu.atom.view.user-view-test
 (:use jiksnu.atom.view.user-view
       [lazytest.describe :only (describe it testing given do-it)]
       [lazytest.expect :only (expect)]
       jiksnu.view))

(describe get-uri)

(describe author-uri)
