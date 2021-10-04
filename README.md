# shape-shifter
Transforms a string pattern into clojure spec.

# Patterns
Inspired by [grape](https://github.com/bfontaine/grape/blob/master/doc/Patterns.md) pattern system.
All supported patterns can be found [here](src/shape_shifter/core.clj) and also can be expanded.

## Expand patterns
```clojure
(require '[shape-shifter.core :refer [pattern->spec *wildcards*]])

(def spec (binding [*wildcards* (merge *wildcards* {"$banana" #{"banana"}})]
                 (pattern->spec "$banana")))
(s/valid? spec "banana")
```
# Examples

## Simple form
```clojure
(require '[shape-shifter.core :refer [pattern->spec]])
(def spec (pattern->spec "($& read-string $&)"))
(s/valid? spec '(-> "1" read-string inc)) => true
```

## Nested forms
```clojure
(require '[shape-shifter.core :refer [pattern->spec]])
(def spec (pattern->spec "(-> $string read-string (+ $number))"))
(s/valid? spec '(-> "1" read-string (+ 10))) => true
```