import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { RegisterService } from '../../../core/services/register.service';
import { MessageService } from 'primeng/api';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let registerServiceSpy: jasmine.SpyObj<RegisterService>;

  beforeEach(async () => {
    registerServiceSpy = jasmine.createSpyObj('RegisterService', ['register', 'verify']);
    registerServiceSpy.register.and.returnValue(of({} as any));
    registerServiceSpy.verify.and.returnValue(of({ verified: true }));

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: RegisterService, useValue: registerServiceSpy },
        MessageService,
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start on step 1', () => {
    expect(component.step).toBe(1);
  });

  it('should have empty username by default', () => {
    expect(component.username).toBe('');
  });

  it('should render username input on step 1', () => {
    const input = fixture.nativeElement.querySelector('input[pInputText]');
    expect(input).toBeTruthy();
  });

  it('should set usernameTouched when createAccount called with empty username', () => {
    component.username = '';
    component.createAccount();
    expect(component.usernameTouched).toBeTrue();
  });

  it('should not advance to step 2 when username is empty', () => {
    component.username = '';
    component.createAccount();
    expect(component.step).toBe(1);
  });

  it('should advance to step 2 when username is provided', () => {
    component.username = 'alice';
    component.createAccount();
    expect(component.step).toBe(2);
  });

  it('should show step 2 content after createAccount with valid username', () => {
    component.username = 'alice';
    component.createAccount();
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    expect(compiled.textContent).toContain('passkey');
  });

  it('should show validation error message when usernameTouched and username empty', () => {
    component.usernameTouched = true;
    component.username = '';
    fixture.detectChanges();
    const errorMsg = fixture.nativeElement.querySelector('small.text-red-500');
    expect(errorMsg).toBeTruthy();
    expect(errorMsg.textContent).toContain('required');
  });

  it('should start with loading false', () => {
    expect(component.loading).toBeFalse();
  });

  it('registerPasskey should call registerService.register with username', () => {
    component.username = 'alice';
    component.registerPasskey();
    expect(registerServiceSpy.register).toHaveBeenCalledWith('alice');
  });

  it('registerPasskey should set loading to false on register error', () => {
    registerServiceSpy.register.and.returnValue(throwError(() => new Error('fail')));
    component.username = 'alice';
    component.registerPasskey();
    expect(component.loading).toBeFalse();
  });
});
