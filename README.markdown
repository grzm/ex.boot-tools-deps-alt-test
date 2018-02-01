# ex.tools-deps-alt-test

Example of interaction between boot-alt-test and boot-tools-deps.

Looks like there's some conflict happening with `deps -B` and `alt-test`.

boot-tools-deps in [build.boot](build.boot):

```clojure
(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [metosin/boot-alt-test "0.3.2" :scope "test"]
                            [seancorfield/boot-tools-deps "0.2.3"]])
```

Here's [deps.edn](deps.edn):

```clojure
{:deps {org.clojure/java.jdbc {:mvn/version "0.7.5"}}
 :aliases {:test
           {:extra-deps {org.clojure/test.check {:mvn/version "0.10.0-alpha2"}}}}}
```

Here's the [test](test/com/grzm/ex/tools_deps_alt_test_test.clj) being run:

```clojure
(ns com.grzm.ex.tools-deps-alt-test-test
  (:require [clojure.test :refer :all]
            [com.grzm.ex.tools-deps-alt-test :refer :all]))

(defn require? [sym]
  (try
    (require sym)
    true
    (catch Exception _
      false)))

(deftest test-requires
  (testing "default deps"
    (is (require? 'clojure.java.jdbc)))
  (testing "extra-deps"
    (is (require? 'clojure.test.check))))

```

I'm comparing the results of `boot test` and `boot alt-test` alone and
with various `deps` middleware options.

    boot [options] test

 options         | `deps` | `extra-deps`
-----------------|:------:|:-----------:
 <none>          | fail   | fail
 deps test       | fail   | fail
 deps -B         |  ok    | fail
 deps -A test    | fail   | fail
 deps -B -A test |  ok    |  ok


    boot [options] alt-test

 options         | `deps` | `extra-deps`
-----------------|:------:|:-----------:
 <none>          | fail   | fail
 deps test       | fail   | fail
 deps -B         |  Ex    |  Ex
 deps -A test    | fail   | fail
 deps -B -A test |  Ex    |  Ex

Using `-B` (`--overwrite-boot-deps`) with `alt-test` raises

    java.io.FileNotFoundException: Could not locate metosin/boot_alt_test/impl__init.class or metosin/boot_alt_test/impl.clj on classpath. Please check that namespaces with dashes use underscores in the Clojure file name.

## License

Public domain
