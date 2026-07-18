(ns jp_ashiba.murakumo
  "Pure cljc actor boundary generated from manifest migration scaffold."
  (:require [clojure.string :as str]))

(def actor-did
  "did:web:jp-ashiba.etzhayyim.com")

(def common-gates
  [:council-charter-attestation
   :no-platform-held-key-baseline
   :no-probing-baseline
   :murakumo-only-inference-baseline
   :did-primary-baseline
   :append-only-gate-baseline
   :kotoba-only-substrate-baseline])

(defn collection
  [name]
  (str "com.etzhayyim.jp-ashiba." name))

(def cell-specs {
  :listcatalog {:legacy-cell "com-etzhayyim-apps-jpAshiba-listCatalog"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "listcatalog")]
     :required-gates common-gates
     :trigger "manifest cell listcatalog"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :checkavailability {:legacy-cell "com-etzhayyim-apps-jpAshiba-checkAvailability"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "checkavailability")]
     :required-gates common-gates
     :trigger "manifest cell checkavailability"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :createquote {:legacy-cell "com-etzhayyim-apps-jpAshiba-createQuote"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "createquote")]
     :required-gates common-gates
     :trigger "manifest cell createquote"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :get {:legacy-cell "com-etzhayyim-apps-jpashiba-coverage-get"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "get")]
     :required-gates common-gates
     :trigger "manifest cell get"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :shinkaevolution {:legacy-cell "com-etzhayyim-apps-standard-shinkaEvolution"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "shinkaevolution")]
     :required-gates common-gates
     :trigger "manifest cell shinkaevolution"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :shinkaknowledge {:legacy-cell "com-etzhayyim-apps-standard-shinkaKnowledge"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "shinkaknowledge")]
     :required-gates common-gates
     :trigger "manifest cell shinkaknowledge"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :shinka {:legacy-cell "shinka"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "shinka")]
     :required-gates common-gates
     :trigger "manifest cell shinka"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :koji {:legacy-cell "koji"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "koji")]
     :required-gates common-gates
     :trigger "manifest cell koji"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :kyumei {:legacy-cell "kyumei"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "kyumei")]
     :required-gates common-gates
     :trigger "manifest cell kyumei"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :domain-knowledge {:legacy-cell "domain-knowledge"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "domain-knowledge")]
     :required-gates common-gates
     :trigger "manifest cell domain-knowledge"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :rentalcontract {:legacy-cell "com-etzhayyim-apps-jpAshiba-rentalContract"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "rentalcontract")]
     :required-gates common-gates
     :trigger "manifest cell rentalcontract"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :inspectionrecord {:legacy-cell "com-etzhayyim-apps-jpAshiba-inspectionRecord"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "inspectionrecord")]
     :required-gates common-gates
     :trigger "manifest cell inspectionrecord"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :subscriptionplan {:legacy-cell "com-etzhayyim-apps-jpAshiba-subscriptionPlan"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "subscriptionplan")]
     :required-gates common-gates
     :trigger "manifest cell subscriptionplan"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
})

(defn safe-rkey
  [s]
  (let [clean (-> (str s)
                  (str/replace #"^did:web:" "")
                  (str/replace #"[^A-Za-z0-9._~-]" "-"))]
    (if (str/blank? clean) "unknown" clean)))

(defn gate-value
  [attestations gate]
  (or (get attestations gate)
      (get attestations (name gate))
      (when (set? attestations) (attestations gate))
      (when (set? attestations) (attestations (name gate)))))

(defn missing-gates
  [spec attestations]
  (->> (:required-gates spec)
       (remove #(boolean (gate-value attestations %)))
       vec))

(defn put-record-effect
  [collection rkey record]
  {:op :mst/put-record
   :actor actor-did
   :collection collection
   :rkey rkey
   :record record})

(defn records-for
  [spec {:keys [records record computed-at request-id]
         :as input}]
  (let [input-records (cond
                        (map? records) records
                        (some? record) {0 record}
                        :else {})
        base {:actorDid actor-did
              :computedAt computed-at
              :legacyCell (:legacy-cell spec)
              :phase (:phase spec)
              :requestId request-id
              :actorBoundary "cljc-migration-scaffold"
              :scaffold true
              :constitutionalStatus "attested-plan"}]
    (map-indexed
     (fn [idx coll]
       (let [record* (merge {:$type coll}
                            base
                            (or (get input-records coll)
                                (get input-records idx)
                                {}))
             rkey (safe-rkey (or (:rkey record*)
                                 (get record* "rkey")
                                 (:tid record*)
                                 request-id
                                 (str (:legacy-cell spec) "-" idx)))]
         {:collection coll
          :record record*
          :rkey rkey}))
     (:collections spec))))

(defn cell-plan
  [cell-key {:keys [attestations] :as input}]
  (let [spec (get cell-specs cell-key)]
    (when-not spec
      (throw (ex-info "unknown cell" {:cell cell-key})))
    (let [missing (missing-gates spec attestations)]
      (merge
       {:cell cell-key
        :legacy-cell (:legacy-cell spec)
        :actor actor-did
        :phase (:phase spec)
        :murakumo-node (:murakumo-node spec)
        :trigger (:trigger spec)
        :ceiling (:ceiling spec)
        :required-gates (:required-gates spec)
        :missing-gates missing}
       (if (seq missing)
         {:status :blocked
          :effects []}
         (let [planned-records (records-for spec input)]
           {:status :ready
            :records (vec planned-records)
            :effects (mapv (fn [{:keys [collection record rkey]}]
                             (put-record-effect collection rkey record))
                           planned-records)}))))))

(defn all-cell-plans
  [input]
  (into {}
        (map (fn [cell-key] [cell-key (cell-plan cell-key input)]))
        (keys cell-specs)))
