(ns jiksnu.routes.admin.auth-routes-test
  
  )

(test-environment-fixture

 (fact "auth admin index"

   (-> "/admin/auth"
       (mock/request :get)
       response-for
       ) =>
         (every-checker
          (comp status/success? :status)

          )

   )
 
 )

