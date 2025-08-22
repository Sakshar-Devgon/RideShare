# Female Mode Feature Testing Guide

## Overview
This document outlines the testing approach for the Female Mode feature in the RideShare app.

## Features Implemented

### 1. User Profile Data Model
- ✅ Created `User.kt` with `gender` (male/female) and `femaleMode` (boolean) fields
- ✅ Default `femaleMode` is `false`

### 2. Profile/Settings Screen
- ✅ Created `ProfileScreen.kt` with gender selection (radio buttons)
- ✅ Added Female Mode toggle switch (only enabled for female users)
- ✅ Added explanatory text for Female Mode functionality
- ✅ Created `ProfileViewModel.kt` for profile data operations

### 3. Navigation Integration
- ✅ Added Profile route to `Routes.kt`
- ✅ Integrated ProfileScreen into navigation graph
- ✅ Added "Profile & Settings" menu item to HomeScreen

### 4. User Registration Updates
- ✅ Modified `SignUpScreen.kt` to collect gender during registration
- ✅ Updated `AuthViewModel.kt` to create user profile in Firestore
- ✅ Added gender validation in signup process

### 5. Ride Creation Logic
- ✅ Updated `Ride.kt` model with `rideType` field ("normal" or "femaleOnly")
- ✅ Modified `RideViewModel.kt` to check user profile and set ride type
- ✅ If `femaleMode = true` and `gender = "female"`, ride is tagged as "femaleOnly"
- ✅ Otherwise, ride is tagged as "normal"

### 6. Ride Filtering Logic
- ✅ Updated `FindRideViewModel.kt` to filter rides based on user gender
- ✅ "femaleOnly" rides only shown to female users
- ✅ "normal" rides shown to all users
- ✅ Added visual indicator for female-only rides in `FindRideScreen.kt`

## Manual Testing Scenarios

### Test Case 1: User Registration with Gender
1. Open the app and navigate to Sign Up
2. Fill in name, email, password, confirm password
3. Select gender (male or female)
4. Verify signup fails if gender is not selected
5. Complete signup and verify user profile is created in Firestore

### Test Case 2: Profile Settings - Male User
1. Sign in as a male user
2. Navigate to Profile & Settings from home menu
3. Verify gender is pre-selected as "male"
4. Verify Female Mode toggle is disabled
5. Try to change gender to female
6. Verify Female Mode toggle becomes enabled
7. Save changes and verify they persist

### Test Case 3: Profile Settings - Female User
1. Sign in as a female user
2. Navigate to Profile & Settings
3. Verify gender is pre-selected as "female"
4. Verify Female Mode toggle is enabled
5. Toggle Female Mode on/off
6. Save changes and verify they persist

### Test Case 4: Ride Creation - Female Mode OFF
1. Sign in as female user with Female Mode OFF
2. Create a ride offer
3. Verify ride is created with rideType = "normal"
4. Check Firestore to confirm ride document

### Test Case 5: Ride Creation - Female Mode ON
1. Sign in as female user with Female Mode ON
2. Create a ride offer
3. Verify ride is created with rideType = "femaleOnly"
4. Check Firestore to confirm ride document

### Test Case 6: Ride Creation - Male User
1. Sign in as male user (Female Mode not applicable)
2. Create a ride offer
3. Verify ride is created with rideType = "normal"
4. Check Firestore to confirm ride document

### Test Case 7: Ride Filtering - Male User
1. Sign in as male user
2. Navigate to Find Rides
3. Verify only "normal" rides are displayed
4. Verify "femaleOnly" rides are NOT displayed
5. Verify no "Female Only Ride" indicators are shown

### Test Case 8: Ride Filtering - Female User
1. Sign in as female user
2. Navigate to Find Rides
3. Verify both "normal" and "femaleOnly" rides are displayed
4. Verify "Female Only Ride" indicator appears for femaleOnly rides
5. Verify pink color indicator for female-only rides

### Test Case 9: Cross-Gender Ride Acceptance
1. Create a "femaleOnly" ride with female user A
2. Sign in as male user B
3. Verify the ride is NOT visible in Find Rides
4. Sign in as female user C
5. Verify the ride IS visible and can be accepted

### Test Case 10: Profile Update Scenarios
1. Female user enables Female Mode
2. Create a ride (should be femaleOnly)
3. Disable Female Mode in profile
4. Create another ride (should be normal)
5. Verify both rides exist with correct types

## Database Verification

### Firestore Collections to Check:

#### users/{userId}
```json
{
  "userId": "user123",
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phoneNumber": "",
  "gender": "female",
  "femaleMode": true
}
```

#### rides/{rideId}
```json
{
  "userId": "user123",
  "userName": "Jane Doe",
  "rideType": "femaleOnly",
  "from": "Location A",
  "to": "Location B",
  // ... other ride fields
}
```

## Expected Behavior Summary

1. **Male users**: Cannot enable Female Mode, all their rides are "normal"
2. **Female users with Female Mode OFF**: All their rides are "normal"
3. **Female users with Female Mode ON**: All their rides are "femaleOnly"
4. **Ride visibility**: 
   - Male users see only "normal" rides
   - Female users see both "normal" and "femaleOnly" rides
5. **UI indicators**: Female-only rides show pink "👩 Female Only Ride" label

## Notes
- This is a prototype implementation with self-selected gender (no identity verification)
- Female Mode setting only affects ride creation, not retroactively
- Existing rides maintain their original rideType
- The feature gracefully handles users without profile data (defaults to normal rides)
