(def project 'com.grzm/ex.tools-deps-alt-test)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [metosin/boot-alt-test "0.3.2" :scope "test"]
                            [com.stuartsierra/dependency "0.2.0"]
                            [seancorfield/boot-tools-deps "0.2.3"]])

(task-options!
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/ex.tools-deps-alt-test"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(require '[adzerk.boot-test :refer [test]])

(require '[metosin.boot-alt-test :refer (alt-test)])

(require '[boot-tools-deps.core :refer [deps]])
