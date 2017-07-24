//mongeez formatted javascript
//changeset unlocker:v1_3
db.firms.remove({});

db.silhouette.user.remove({});

db.silhouette.user.insert({
                          	"_id" : "aeadceec-7dcc-4b1f-9351-2236f0aa5977",
                          	"loginInfo" : {
                          		"providerID" : "credentials",
                          		"providerKey" : "test@test.test"
                          	},
                          	"firstName" : "test",
                          	"firmID" : "aeadceec-7dcc-4b1f-9351-2236f0aa6001",
                          	"lastName" : "test",
                          	"fullName" : "test test",
                          	"email" : "test@test.test",
                          	"activated" : true,
                          	"blocked" : false,
                          	"position": "cashier",
                          	"roles": []
                          });

db.firms.insert({
                	"_id" : "aeadceec-7dcc-4b1f-9351-2236f0aa6001",
                	"name" : "ООО Фирма",
                	"taxVariant" : "BASE_TAX_VARIANT",
                	"taxIdentityNumber" : "1234567890",
                	"legalAddress" : "123456, г. Мухопёрово, ул. Мухоморная, д.15",
                	"blocked" : false
                });

db.sequences.insert({"_id": "orders", "seq": 0});