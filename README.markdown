# ex.tools-deps-alt-test

Example of interaction between boot-alt-test and boot-tools-deps.

Looks like there's some conflict happening with `deps -B` and `alt-test`.

boot-tools-deps in [build.boot](build.boot):

```clojure
(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [metosin/boot-alt-test "0.3.2" :scope "test"]
                            [com.stuartsierra/dependency "0.2.0"]
                            [seancorfield/boot-tools-deps "0.2.3"]])
```

Here's [deps.edn](deps.edn):

```clojure
{:deps {org.clojure/java.jdbc {:mvn/version "0.7.5"}}
 :aliases {:test
           {:extra-deps {org.clojure/math.combinatorics {:mvn/version "0.1.4"}}}}}
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
  (testing ":dependencies"
    (is (require? 'com.stuartsierra.dependency)))
  (testing ":deps"
    (is (require? 'clojure.java.jdbc)))
  (testing ":extra-deps"
    (is (require? 'clojure.math.combinatorics))))
```

I'm comparing the results of `boot test` and `boot alt-test` alone and
with various `deps` middleware options.

*If actual results differ from my expectations (which may differ from
what is intended), **actual results are included in parens.***

One of my expectations is to be able to merge `deps.edn` `:deps` and
`build.boot` `:dependencies` so I can keep Boot task-related
dependencies out of the general project dependencies in deps.edn. I
enjoy the option of using multiple, independent tools with a project,
and while aliasing in deps can work well for keeping things separate,
it's even cleaner to be able to have tool-associated deps directly
associated with the tool.

    boot [options] test


 options           | `:dependencies`  |`:deps`    | `:extra-deps`
-------------------|:----------------:|:---------:|:---------------:
 *none*            | ok               | fail      | fail
 `deps`            | ok               | ok (fail) | fail
 `deps -A test`    | ok               | ok (fail) | ok (fail)
 `deps -B -A test` | fail             | ok        | ok
 `deps -B`         | fail             | ok        | fail


    boot [options] alt-test

 options           | `:dependencies`  |`:deps`     | `:extra-deps`
-------------------|:----------------:|:----------:|:---------------:
 *none*            | ok               | fail       | fail
 `deps`            | ok               | ok (fail)  | fail
 `deps -A test`    | ok               | ok (fail)  | ok (fail)
 `deps -B -A test` | fail (Error)     | ok (Error) | ok (Error)
 `deps -B`         | fail (Error)     | ok (Error) | fail (Error)

Using `-B` (`--overwrite-boot-deps`) with `alt-test` raises

> java.io.FileNotFoundException: Could not locate
> metosin/boot_alt_test/impl__init.class or
> metosin/boot_alt_test/impl.clj on classpath. Please check that
> namespaces with dashes use underscores in the Clojure file name.

There's an inconsistency between the behavior of `boot test` and `boot
alt-test`. `boot deps -B test` does not throw this exception, while
`boot deps -B alt-test` does. It makes sense for the dependencies to
be available while reading `build.boot` so any `require` statements in
the body of may succeed even when the `-B` flag is present. From this
perspective, `boot test` is working as expected, and the issue is in
the interaction between boot-tools-deps and boot-alt-test.

## License

Public domain
