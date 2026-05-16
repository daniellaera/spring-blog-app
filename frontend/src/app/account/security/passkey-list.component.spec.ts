import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PasskeyListComponent } from './passkey-list.component';
import { PasskeyService } from './passkey.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PasskeyDto } from './passkey.dto';

const mockPasskeys: PasskeyDto[] = [
  {
    id: 'pk-1',
    deviceName: 'My iPhone',
    createdAt: '2024-01-01T00:00:00Z',
    lastUsedAt: '2024-06-01T00:00:00Z',
    backedUp: true,
    transports: ['internal'],
    aaguid: null,
  },
  {
    id: 'pk-2',
    deviceName: 'USB Key',
    createdAt: '2024-02-01T00:00:00Z',
    lastUsedAt: null,
    backedUp: false,
    transports: ['usb'],
    aaguid: null,
  },
];

describe('PasskeyListComponent', () => {
  let component: PasskeyListComponent;
  let fixture: ComponentFixture<PasskeyListComponent>;
  let passkeyServiceSpy: jasmine.SpyObj<PasskeyService>;

  async function setup(passkeys: PasskeyDto[] | null = mockPasskeys) {
    // Deep-clone to prevent tests from mutating shared constants
    const cloned = passkeys !== null ? passkeys.map(p => ({ ...p })) : null;
    passkeyServiceSpy = jasmine.createSpyObj('PasskeyService', ['list', 'rename', 'delete', 'register']);
    passkeyServiceSpy.list.and.returnValue(
      cloned !== null ? of(cloned) : throwError(() => new Error('Failed'))
    );
    passkeyServiceSpy.rename.and.returnValue(of({ ...mockPasskeys[0], deviceName: 'New Name' }));
    passkeyServiceSpy.delete.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [PasskeyListComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: PasskeyService, useValue: passkeyServiceSpy },
        MessageService,
        ConfirmationService,
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PasskeyListComponent);
    component = fixture.componentInstance;
  }

  it('should create', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    expect(component).toBeTruthy();
  }));

  it('should have loading true before ngOnInit', async () => {
    await setup();
    expect(component.loading).toBeTrue();
  });

  it('should set loading to false after passkeys load', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    expect(component.loading).toBeFalse();
  }));

  it('should display passkeys after load', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    expect(component.passkeys.length).toBe(2);
    expect(component.passkeys[0].deviceName).toBe('My iPhone');
  }));

  it('should show empty array when no passkeys returned', fakeAsync(async () => {
    await setup([]);
    fixture.detectChanges();
    tick();
    expect(component.passkeys).toEqual([]);
    expect(component.loading).toBeFalse();
  }));

  it('should set loading to false on load error', fakeAsync(async () => {
    await setup(null);
    fixture.detectChanges();
    tick();
    expect(component.loading).toBeFalse();
  }));

  it('openRenameDialog should set editingKey and editingName', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    const firstKey = component.passkeys[0];
    component.openRenameDialog(firstKey);
    expect(component.editingKey).toEqual(firstKey);
    expect(component.editingName).toBe('My iPhone');
    expect(component.renameDialogVisible).toBeTrue();
  }));

  it('saveRename should call passkeyService.rename with new name', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    component.editingKey = component.passkeys[0];
    component.editingName = 'New Name';
    component.saveRename();
    expect(passkeyServiceSpy.rename).toHaveBeenCalledWith('pk-1', 'New Name');
  }));

  it('saveRename should update passkey in list on success', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    component.editingKey = component.passkeys[0];
    component.editingName = 'New Name';
    passkeyServiceSpy.rename.and.returnValue(of({ ...mockPasskeys[0], deviceName: 'New Name' }));
    component.saveRename();
    tick();
    expect(component.passkeys[0].deviceName).toBe('New Name');
    expect(component.renameDialogVisible).toBeFalse();
  }));

  it('deletePasskey should remove passkey from list', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    const initial = component.passkeys.length;
    component.deletePasskey(component.passkeys[0]);
    tick();
    expect(component.passkeys.length).toBe(initial - 1);
  }));

  it('deletePasskey should call passkeyService.delete with passkey id', fakeAsync(async () => {
    await setup();
    fixture.detectChanges();
    tick();
    component.deletePasskey(mockPasskeys[0]);
    expect(passkeyServiceSpy.delete).toHaveBeenCalledWith('pk-1');
  }));

  it('getDeviceIcon returns mobile icon for internal transport', () => {
    expect(PasskeyListComponent.prototype.getDeviceIcon(['internal'])).toBe('pi pi-mobile');
  });

  it('getDeviceIcon returns key icon for usb transport', () => {
    expect(PasskeyListComponent.prototype.getDeviceIcon(['usb'])).toBe('pi pi-key');
  });

  it('getDeviceIcon returns shield icon for unknown transport', () => {
    expect(PasskeyListComponent.prototype.getDeviceIcon([])).toBe('pi pi-shield');
  });
});
