@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    User user = authService.login(request.getUsername(), request.getPassword());

    LoginResponse resp = new LoginResponse();

    if (user == null) {
        resp.setSuccess(false);
        resp.setMessage("Sai tài khoản hoặc mật khẩu");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    resp.setSuccess(true);
    resp.setMessage("Đăng nhập thành công");
    resp.setUserID(BigInteger.valueOf(user.getUserId())); // đúng với field trong entity
    resp.setEmail(user.getEmail());
    resp.setFullName(user.getFullName());

    return ResponseEntity.ok(resp);
}
