# ex.tools-deps-alt-test

**Note: this exploration was motivated by the behavior of
boot-tools-deps 0.2.3. The `-Q` quick-merge option [added in
boot-tools-deps 0.3.0][quick-merge-commit] addresses the expectations
I express below. Thanks, Sean, for the fast feedback!**

Example of interaction between [boot-test][], [boot-alt-test][], and
[boot-tools-deps][].

[boot-test]: https://github.com/adzerk-oss/boot-test
[boot-alt-test]: https://github.com/metosin/boot-alt-test
[boot-tools-deps]: https://github.com/seancorfield/boot-tools-deps
[quick-merge-commit]: https://github.com/seancorfield/boot-tools-deps/commit/b51fc6e0ec8185f0e5ad4f90be9e6913fe2a5d5d

boot-tools-deps in [build.boot](build.boot):

```clojure
(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [metosin/boot-alt-test "0.3.2" :scope "test"]
                            [com.stuartsierra/dependency "0.2.0"]
                            #_[seancorfield/boot-tools-deps "0.2.3"] ;; original
                            [seancorfield/boot-tools-deps "0.3.0"]])
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

One of my expectations is to be able to merge `deps.edn` `:deps` and
`build.boot` `:dependencies` so I can keep Boot task-related
dependencies out of the general project dependencies in deps.edn. I
enjoy the option of using multiple, independent tools with a project,
and while aliasing in deps can work well for keeping things separate,
it's even cleaner to be able to have tool-associated deps directly
associated with the tool. **The `-Q` quick-merge option introduced in
boot-tools-deps 0.3.0 provides this feature.**

    boot [options] test

 options           | `:dependencies`  |`:deps`    | `:extra-deps`
-------------------|:----------------:|:---------:|:---------------:
 *none*            | ok               | fail      | fail
 `deps`            | ok               | fail ➊    | fail
 `deps -Q`         | ok/Error ➋       | ok/Error  | fail/Error
 `deps -Q -A test` | ok               | ok        | ok
 `deps -A test`    | ok               | fail ➊     | fail
 `deps -B`         | fail             | ok        | fail
 `deps -B -A test` | fail             | ok        | ok
 `deps -B -Q`      | Error ➌          | Error     | Error

➊ Note that `deps` and `deps -A` add the libraries in `deps.edn`
to the classpath, but does not merge them into the Boot environment
`:dependencies`, which is why the `:deps` and `:extra-deps` tests
fail.

➋ About 50% of the time (5 of 11 runs), `deps -Q` throws
`java.lang.ExceptionInInitializerError`. Full stack trace is in
[exception-initializer-error.txt](exception-initializer-error.txt). I
didn't observe this in 10 runs with boot-alt-test, and haven't dug
into this further.

    λ boot --version
    #http://boot-clj.com
    #Fri Feb 02 15:20:28 CST 2018
    BOOT_VERSION=2.7.2
    BOOT_CLOJURE_VERSION=1.10.0-alpha2
    BOOT_CLOJURE_NAME=org.clojure/clojure
    λ uname -v
    Darwin Kernel Version 17.4.0: Sun Dec 17 09:19:54 PST 2017; root:xnu-4570.41.2~1/RELEASE_X86_64
    λ system_profiler SPSoftwareDataType | ack -v Name
    Software:

        System Software Overview:

          System Version: macOS 10.13.3 (17D47)
          Kernel Version: Darwin 17.4.0
          Boot Volume: Macintosh HD
          Boot Mode: Normal
          Secure Virtual Memory: Enabled
          System Integrity Protection: Enabled
          Time since boot: 7 days 18:11

➌ The `-B` and `-Q` flags are exclusive: boot-tools-deps raises an
AssertionError if they are both specified.

    boot [options] alt-test

 options           | `:dependencies`  |`:deps`     | `:extra-deps`
-------------------|:----------------:|:----------:|:---------------:
 *none*            | ok               | fail       | fail
 `deps`            | ok               | fail       | fail
 `deps -Q`         | ok               | ok         | fail
 `deps -Q -A test` | ok               | ok         | ok
 `deps -A test`    | ok               | fail       | fail
 `deps -B -A test` | fail (Error)     | ok (Error) | ok (Error)
 `deps -B`         | fail (Error)     | ok (Error) | fail (Error)
 `deps -B -Q`      | Error            | Error      | Error

Using `-B` (`--overwrite-boot-deps`) with `alt-test` raises

> java.io.FileNotFoundException: Could not locate
> metosin/boot_alt_test/impl__init.class or
> metosin/boot_alt_test/impl.clj on classpath. Please check that
> namespaces with dashes use underscores in the Clojure file name.

Sean hypothesized that the reason for this is that boot-test is
completely loaded when build.boot is read, while `boot-alt-test.impl`
is required only after the pod is loaded. As it's no longer available
as a dependency (because the initial `build.boot` dependencies were
overwritten as specified with the `-B` flag, we get the above
exception.

## License

Public domain
