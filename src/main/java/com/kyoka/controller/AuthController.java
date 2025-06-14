package com.kyoka.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.kyoka.dto.UserDTO;
import com.kyoka.exception.ResourceNotFoundException;
import com.kyoka.model.AppRole;
import com.kyoka.model.Cart;
import com.kyoka.model.Role;
import com.kyoka.model.User;
import com.kyoka.repository.CartRepository;
import com.kyoka.repository.RoleRepository;
import com.kyoka.repository.UserRepository;
import com.kyoka.security.dto.*;
import com.kyoka.security.jwt.JwtUtils;
import com.kyoka.security.UserDetailsImpl;
import com.kyoka.service.impl.GoogleTokenVerifier;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        LoginResponse response = new LoginResponse(userDetails.getId(), jwtToken, userDetails.getUsername(), roles);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUserName(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "owner":
                        Role ownerRole = roleRepository.findByRoleName(AppRole.ROLE_RESTAURANT_OWNER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(ownerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        // Create a cart
        Cart cart = new Cart();
        cart.setUser(user);
        Cart savedCart = cartRepository.save(cart);

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> currentUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        UserDTO userDTO = new UserDTO(
                user.getUserId(),
                user.getUserName(),
                roles,
                user.getFavoriteRestaurants(),
                user.getEmail(),
                user.getAddresses()
        );

        return ResponseEntity.ok().body(userDTO);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser() {
        return ResponseEntity.ok(new MessageResponse("You've been logged out successfully"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            // Verify Google ID token
            GoogleIdToken idToken = googleTokenVerifier.verifyToken(request.getIdToken());
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Extract user information from Google token
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Find or create user in our system
            User user = findOrCreateGoogleUser(email, name);

            // Generate our JWT token
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .toList();

            LoginResponse response = new LoginResponse(user.getUserId(), jwtToken, user.getUserName(), roles);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Google login failed: " + e.getMessage());
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
    }

    private User findOrCreateGoogleUser(String email, String name) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user for Google login
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUserName(name);
        // No password needed for Google users
        newUser.setPassword(encoder.encode("GOOGLE_USER")); // Placeholder password

        // Assign default customer role
        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByRoleName(AppRole.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(customerRole);
        newUser.setRoles(roles);

        User savedUser = userRepository.save(newUser);

        // Create cart for new user
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);

        return savedUser;
    }


//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse> resetPassword(
//            @RequestBody ResetPasswordRequest req) throws UserException {
//
//        PasswordResetToken resetToken = passwordResetTokenService.findByToken(req.getToken());
//
//        if (resetToken == null ) {
//            throw new UserException("token is required...");
//        }
//        if(resetToken.isExpired()) {
//            passwordResetTokenService.delete(resetToken);
//            throw new UserException("token get expired...");
//
//        }
//
//        // Update user's password
//        User user = resetToken.getUser();
//        userService.updatePassword(user, req.getPassword());
//
//        // Delete the token
//        passwordResetTokenService.delete(resetToken);
//
//        ApiResponse res=new ApiResponse();
//        res.setMessage("Password updated successfully.");
//        res.setStatus(true);
//
//        return ResponseEntity.ok(res);
//    }
//
//    @PostMapping("/reset-password-request")
//    public ResponseEntity<ApiResponse> resetPassword(@RequestParam("email") String email) throws UserException {
//        User user = userService.findUserByEmail(email);
//        System.out.println("ResetPasswordController.resetPassword()");
//
//        if (user == null) {
//            throw new UserException("user not found");
//        }
//
//        userService.sendPasswordResetEmail(user);
//
//        ApiResponse res=new ApiResponse();
//        res.setMessage("Password reset email sent successfully.");
//        res.setStatus(true);
//
//        return ResponseEntity.ok(res);
//    }
}