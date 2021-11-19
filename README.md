# shape-shifter
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.clj-holmes/shape-shifter.svg)](https://clojars.org/org.clojars.clj-holmes/shape-shifter)

Transforms a string pattern into clojure spec.

# Patterns
Inspired by [grape](https://github.com/bfontaine/grape/blob/master/doc/Patterns.md) pattern system.

Inspired by [grasp](https://github.com/borkdude/grasp/) spec utilization.

## Wildcards
| Wildcard | Definition |
|----------|------------|
| $        | matches one element of any kind |      
| $fn      | matches one function element |
| $macro-keyword | matches one macro keyword element|
| $symbol | matches one symbol element |
| $string | matches one string element |
| $set    | matches one set element |
| $char   | matches one char element |
| $keyword| matches one keyword element |
| $map    | matches one map element |
| $number | matches one number element |
| $list   | matches one list element |
| $vector | matches one vector element |
| $regex  | matches one regex element |

If an & is provided in the end of a wildcard it'll match 0 or n elements of the specified kind.

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
## Expand patterns
```clojure
(require '[shape-shifter.core :refer [pattern->spec *wildcards*]])

(def spec (binding [*wildcards* (merge *wildcards* {"$banana" #{"banana"}})]
                 (pattern->spec "$banana")))
(s/valid? spec "banana")
```
## Regex evaluation
```clojure
(require '[shape-shifter.core :refer [pattern->spec *wildcards*]])

(def spec (binding [*config* (assoc *config* :interpret-regex? true)]
                 (pattern->spec "#\"bana.*\" ")))
(s/valid? spec "banana")
```

