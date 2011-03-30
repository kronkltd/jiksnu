(ns jiksnu.controller.activity-controller)

(defaction index
  [& options]
  (model.activity/index))

(defaction show
  [id]
  (model.activity/show id))

(defaction user-timeline
  [id]
  (let [user (model.user/fetch-by-id id)]
    [user (model.activity/index :authors (make-id id))]))

(defaction delete
  [id]
  (model.activity/delete id))
