(defproject org.clojars.clj-holmes/shape-shifter "0.3.6"
  :url "https://github.com/clj-holmes/shape-shifter"
  :description "Transforms a string pattern into clojure spec."
  :scm {:name "git"
        :url  "https://github.com/clj-holmes/shape-shifter"}
  :license {:name "Eclipse Public License 1.0"
            :url  "http://opensource.org/licenses/eclipse-1.0.php"}

  :plugins [[lein-ancient "0.7.0"]
            [lein-cljfmt "0.8.0"]
            [lein-shell "0.5.0"]
            [lein-kibit "0.1.8"]
            [lein-cloverage "1.2.2"]
            [lein-nsorg "0.3.0"]
            [jonase/eastwood "0.9.9"]]

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_pass
                                    :sign-releases false}]]

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [org.babashka/sci "0.2.7"]
                 [carocad/parcera "0.11.6"]
                 [org.antlr/antlr4-runtime "4.9.3"]]

  :profiles {:uberjar {:global-vars {*assert* false}
                       :jvm-opts    ["-Dclojure.compiler.direct-linking=true"
                                     "-Dclojure.spec.skip-macros=true"]
                       :main        shape-shifter.core
                       :aot         :all}}
  :main        shape-shifter.core

  :aliases {"lint"            ["do" ["cljfmt" "check"] ["kibit"] ["nsorg"] ["eastwood" "{:namespaces [:source-paths]}"]]
            "lint-fix"        ["do" ["cljfmt" "fix"] ["kibit" "--replace"] ["nsorg" "--replace"]]
            "native"          ["shell" "native-image" "--report-unsupported-elements-at-runtime"
                               "--initialize-at-build-time" "--no-fallback" "-H:+ReportExceptionStackTraces"
                               "-jar" "./target/${:uberjar-name:-${:name}-${:version}-standalone.jar}"
                               "-H:Name=./target/${:name}"]
            "project-version" ["shell" "echo" "${:version}"]})
