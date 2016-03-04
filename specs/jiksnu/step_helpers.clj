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

(defmacro When
  [pattern bind & body]
  (list 'this-as 'this
        (list '.When 'this pattern
              (apply list 'fn bind body))))

(defmacro Then
  [pattern bind & body]
  (list 'this-as 'this
        (list '.Then 'this pattern
              (apply list 'fn bind body))))

(defmacro And
  [pattern bind & body]
  (list 'this-as 'this
        (list '.And 'this pattern
              (apply list 'fn bind body))))
