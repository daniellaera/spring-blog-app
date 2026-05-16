import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { LoginService } from '../../../core/services/login.service';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let loginServiceSpy: jasmine.SpyObj<LoginService>;

  beforeEach(async () => {
    loginServiceSpy = jasmine.createSpyObj('LoginService', ['startLogin', 'verify', 'login', 'getSession']);
    loginServiceSpy.startLogin.and.returnValue(of({} as any));
    loginServiceSpy.verify.and.returnValue(of({ token: 'tok' }));
    loginServiceSpy.getSession.and.returnValue(of(null as any));

    await TestBed.configureTestingModule({
      imports: [LoginComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParams: {} } } },
        { provide: LoginService, useValue: loginServiceSpy },
        MessageService,
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render username input', () => {
    const input = fixture.nativeElement.querySelector('input[pInputText]');
    expect(input).toBeTruthy();
  });

  it('should have empty username by default', () => {
    expect(component.username).toBe('');
  });

  it('should start with no validation errors', () => {
    expect(component.usernameTouched).toBeFalse();
  });

  it('should set usernameTouched and not call service when username is empty', async () => {
    component.username = '';
    await component.signInWithPasskey();
    expect(component.usernameTouched).toBeTrue();
    expect(loginServiceSpy.startLogin).not.toHaveBeenCalled();
  });

  it('should not show error message when username is not touched', () => {
    fixture.detectChanges();
    const errorMsg = fixture.nativeElement.querySelector('small.text-red-500');
    expect(errorMsg).toBeFalsy();
  });

  it('should show error message when username touched and empty', () => {
    component.usernameTouched = true;
    component.username = '';
    fixture.detectChanges();
    const errorMsg = fixture.nativeElement.querySelector('small.text-red-500');
    expect(errorMsg).toBeTruthy();
  });

  it('recoveryVisible getter returns false when recoveryStep is 0', () => {
    component.recoveryStep = 0;
    expect(component.recoveryVisible).toBeFalse();
  });

  it('recoveryVisible getter returns true when recoveryStep is 1', () => {
    component.recoveryStep = 1;
    expect(component.recoveryVisible).toBeTrue();
  });

  it('recoveryVisible setter sets recoveryStep to 0 when set to false', () => {
    component.recoveryStep = 1;
    component.recoveryVisible = false;
    expect(component.recoveryStep).toBe(0);
  });

  it('openRecovery sets recoveryStep to 1 and copies username', () => {
    component.username = 'alice';
    component.openRecovery();
    expect(component.recoveryStep).toBe(1);
    expect(component.recoveryUsername).toBe('alice');
  });

  it('should call loginService.startLogin with username on sign-in attempt', fakeAsync(() => {
    component.username = 'alice';
    component.signInWithPasskey();
    tick(100);
    expect(loginServiceSpy.startLogin).toHaveBeenCalledWith('alice');
  }));

  it('should set loading to false after failed login attempt', fakeAsync(() => {
    loginServiceSpy.startLogin.and.returnValue(throwError(() => new Error('network error')));
    component.username = 'alice';
    component.signInWithPasskey();
    tick(100);
    expect(component.loading).toBeFalse();
  }));
});
