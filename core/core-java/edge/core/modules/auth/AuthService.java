package edge.core.modules.auth;

import javax.jws.WebService;

import edge.core.modules.common.EdgeResponse;

@WebService
public interface AuthService {

	EdgeResponse<SignUpEntity> signUp(SignUpEntity signUpEntity);
	
	EdgeResponse<SignUpEntity> resetPassword(SignUpEntity signUpEntity);

	String sendVerificationCode(String emailId);

}
