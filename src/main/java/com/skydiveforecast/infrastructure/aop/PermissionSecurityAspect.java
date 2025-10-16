package com.skydiveforecast.infrastructure.aop;

import com.skydiveforecast.domain.service.PermissionSecurityService;
import com.skydiveforecast.infrastructure.security.PermissionSecurity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionSecurityAspect {

    private final PermissionSecurityService permissionSecurityService;

    @Around("@annotation(com.skydiveforecast.infrastructure.security.PermissionSecurity)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PermissionSecurity permissionSecurityAnnotation = method.getAnnotation(PermissionSecurity.class);

        if (permissionSecurityAnnotation != null) {
            String permission = permissionSecurityAnnotation.permission();
            if (!permissionSecurityService.hasPermission(permission)) {
                throw new AccessDeniedException("Access Denied: You do not have the required permission '" + permission + "'");
            }
        } else {
            throw new AccessDeniedException("Access Denied: PermissionSecurity annotation not found on method.");
        }

        return joinPoint.proceed();
    }
}