#!/usr/bin/env nbb
;; run_tests.cljs — jp-ashiba actor の検査。
;;
;;   nbb --classpath src:test run_tests.cljs             gate + 構造 + 固定値（network 不要）
;;   nbb --classpath src:test run_tests.cljs --network   claims の測定値を実際に取りに行く
;;
;; この repo は 2026-06 の snapshot 以来 **テストを 1 本も持っていなかった**。
;; 2026-07-18 の rescue commit で入った `src/jp_ashiba/murakumo.cljc` は
;; deny-by-default の gate（required-gates が揃わなければ effect を出さない）を
;; 持つのに、それが緩んでも誰も気づかない状態が既定だった。
;;
;; workspace の規則（superproject CLAUDE.md）で script host は nbb に一本化されて
;; おり、新規の .ts / .mjs / .sh は禁止。よって runner は nbb + cljs.test である。
(ns run-tests
  (:require [clojure.test :as t]
            [jp_ashiba.gate-test]
            [jp_ashiba.repo-test]
            [jp_ashiba.network-test :as network]))

(def green-marker
  "scripts/maturity-loop/mutations.edn の `:green-marker`。
   全部緑のときだけ出る —— 出力に現れるかどうかで mutation が噛んだかを判定する
   ので、緑でないときに印字してはならない。"
  "jp-ashiba actor: all green")

(defmethod t/report [:cljs.test/default :end-run-tests] [m]
  (if (t/successful? m)
    (println (str "\nmode: " (if network/enabled? "offline + network" "offline") "\n" green-marker))
    (do (println "\njp-ashiba actor: FAILED")
        (js/process.exit 1))))

(t/run-tests 'jp_ashiba.gate-test
             'jp_ashiba.repo-test
             'jp_ashiba.network-test)
