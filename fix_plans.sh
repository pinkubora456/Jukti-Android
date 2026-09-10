#!/bin/bash

# We will use sed to replace the getters in FirebaseRepository.kt for fetchAllPlans and observePlans.
sed -i 's/doc.getLong("id")/doc.get("id")?.toString()?.toDoubleOrNull()?.toLong()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("planName")/doc.get("planName")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("googlePlayProductId")/doc.get("googlePlayProductId")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("planValidity")/doc.get("planValidity")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("offerValidity")/doc.get("offerValidity")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getBoolean("isLifetime")/doc.get("isLifetime")?.toString()?.toBooleanStrictOrNull()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("validityType")/doc.get("validityType")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getLong("validityValue")/doc.get("validityValue")?.toString()?.toDoubleOrNull()?.toLong()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("validityLabel")/doc.get("validityLabel")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("planPrice")/doc.get("planPrice")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("discount")/doc.get("discount")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("finalPrice")/doc.get("finalPrice")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("features")/doc.get("features")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("contents")/doc.get("contents")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getBoolean("isActive")/doc.get("isActive")?.toString()?.toBooleanStrictOrNull()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("imageUrl")/doc.get("imageUrl")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getString("examTarget")/doc.get("examTarget")?.toString()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getLong("createdAt")/doc.get("createdAt")?.toString()?.toDoubleOrNull()?.toLong()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getLong("updatedAt")/doc.get("updatedAt")?.toString()?.toDoubleOrNull()?.toLong()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
sed -i 's/doc.getBoolean("guidanceEnabled")/doc.get("guidanceEnabled")?.toString()?.toBooleanStrictOrNull()/g' app/src/main/java/com/example/data/repository/FirebaseRepository.kt
