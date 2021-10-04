(defproject org.clojars.clj-holmes/shape-shifter "0.1.1"
  :url "https://github.com/clj-holmes/shape-shifter"
  :description "Transforms a string pattern into clojure spec."
  :scm {:name "git"
        :url  "https://github.com/clj-holmes/clj-holmes"}
  :license {:name "Eclipse Public License 1.0"
            :url  "http://opensource.org/licenses/eclipse-1.0.php"}

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.6.4"]
            [lein-nsorg "0.3.0"]
            [jonase/eastwood "0.3.10"]]
            
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :sign-releases false}]]

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [carocad/parcera "0.11.6"]
                 [org.antlr/antlr4-runtime "4.7.1"]]

  :profiles {:uberjar {:global-vars {*assert* false}
                       :jvm-opts    ["-Dclojure.compiler.direct-linking=true"
                                     "-Dclojure.spec.skip-macros=true"]
                       :aot         :all
                       :main        shape-shifter.core}}

  :aot :all

  :aliases {"lint"       ["do" ["cljfmt" "check"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"   ["do" ["cljfmt" "fix"] ["nsorg" "--replace"]]})
