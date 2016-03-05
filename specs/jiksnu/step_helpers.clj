(ns jiksnu.step-helpers)

(defmacro step-definitions
  [& body]
  (list 'set! (list '.-exports 'js/module)
        (apply list 'fn [] body)))

(defmacro Given
  [pattern bind & body]
  (list 'this-as 'this
        (list '.Given 'this pattern
              (apply list 'fn bind body))))
