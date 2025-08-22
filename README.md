# 🚗 RideShare - Android Carpooling App

A modern, feature-rich ride-sharing application built with Kotlin and Jetpack Compose, designed to connect users for safe and convenient carpooling experiences.

## ✨ Features

### 🔐 **User Authentication**
- Secure Firebase Authentication
- User profile management with gender preferences
- Phone number verification

### 🚗 **Ride Management**
- **Offer Rides**: Create ride offers with pickup/drop locations, timing, and pricing
- **Find Rides**: Browse and filter available rides
- **Accept Rides**: Join rides with instant notifications
- **Ride History**: Track completed journeys

### 👩 **Safety Features**
- **Female Mode**: Women can create female-only rides for enhanced safety
- **Gender-based Filtering**: Automatic ride filtering based on user preferences
- **User Verification**: Profile-based user validation

### 🎨 **Modern UI/UX**
- Material Design 3 implementation
- Adaptive Light/Dark theme support
- Smooth animations and transitions
- Responsive design for all screen sizes

### 🗺️ **Location Services**
- OpenStreetMap integration
- Real-time location tracking
- Route visualization
- Address autocomplete

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModels and LiveData
- **Backend**: Firebase (Authentication, Firestore)
- **Maps**: OpenStreetMap (OSMDroid)
- **Navigation**: Navigation Compose
- **Design**: Material Design 3

## 📱 Screenshots

*Add your app screenshots here*

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Firebase project setup

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/RideShare.git
   cd RideShare
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Enable Authentication and Firestore Database
   - Download `google-services.json` from your Firebase project
   - Place it in the `app/` directory (use `app/google-services.json.template` as reference)

3. **Configure Firestore Security Rules**
   ```bash
   firebase login
   firebase use YOUR_PROJECT_ID
   firebase deploy --only firestore:rules
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 🔧 Configuration

### Firebase Setup
1. Enable Email/Password authentication in Firebase Console
2. Create Firestore database with the following collections:
   - `users` - User profiles
   - `rides` - Active ride offers
   - `rideHistory` - Completed rides

### Security Rules
The project includes pre-configured Firestore security rules in `firestore.rules` that ensure:
- Users can only access their own data
- Proper ride acceptance permissions
- Secure ride history access

## 📂 Project Structure

```
app/src/main/java/com/beast/rideshare/
├── Model/                  # Data models
├── Navigation/             # Navigation setup
├── ui/
│   ├── Screens/           # All app screens
│   │   ├── Auth/          # Authentication screens
│   │   ├── Home/          # Home screen
│   │   ├── Profile/       # Profile management
│   │   └── RideScreens/   # Ride-related screens
│   └── theme/             # App theming
└── MainActivity.kt        # Main activity
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Developer

**Sakshar Devgon**
- Email: sakshardevgon08@gmail.com
- GitHub: [@yourusername](https://github.com/yourusername)

## 🙏 Acknowledgments

- Firebase for backend services
- OpenStreetMap for mapping services
- Material Design team for design guidelines
- Jetpack Compose team for the modern UI toolkit
