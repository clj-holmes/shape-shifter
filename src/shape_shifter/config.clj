(ns shape-shifter.config)

;; hash-map with global configs to shape-shifter
(def ^:dynamic *config* {:interpret-regex? false})

;; hash-map with all wildcards that can be used
;; and the respective function it will translate too.
(def ^:dynamic *wildcards*
  {"$"              `any?
   "$fn"            `(s/cat :function #(= 'fn %)
                            :args (s/coll-of symbol? :kind vector?)
                            :body any?)
   "$macro-keyword" `qualified-keyword?
   "$symbol"        `symbol?
   "$string"        `string?
   "$set"           `set?
   "$char"          `char?
   "$keyword"       `keyword?
   "$map"           `map?
   "$number"        `number?
   "$list"          `list?
   "$vector"        `vector?
   "$regex"         `s/regex?})
