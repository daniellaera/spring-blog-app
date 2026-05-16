import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PostDetailComponent } from './post-detail.component';
import { PostService } from '../../../core/services/post.service';
import { LoginService } from '../../../core/services/login.service';
import { CommentService } from '../../../core/services/comment.service';
import { ActivatedRoute } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PostDTO } from '../../../dto/post.dto';
import { signal } from '@angular/core';

const mockPost: PostDTO = {
  id: 42,
  title: 'Test Post',
  content: 'Hello world',
  author: 'alice',
  comments: [],
};

function makeLoginService(user: string | null): Partial<LoginService> {
  return {
    isLoggedIn: signal(!!user).asReadonly(),
    currentUser: signal<string | null>(user).asReadonly(),
  };
}

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  let postServiceSpy: jasmine.SpyObj<PostService>;
  let commentServiceSpy: jasmine.SpyObj<CommentService>;

  async function setup(postId: string, post$: any, currentUser: string | null = null) {
    postServiceSpy = jasmine.createSpyObj('PostService', ['getById', 'delete']);
    postServiceSpy.getById.and.returnValue(post$);
    postServiceSpy.delete.and.returnValue(of(undefined));

    commentServiceSpy = jasmine.createSpyObj('CommentService', ['getComments', 'createComment']);
    commentServiceSpy.getComments.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [PostDetailComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { params: { postId } } } },
        { provide: PostService, useValue: postServiceSpy },
        { provide: LoginService, useValue: makeLoginService(currentUser) },
        { provide: CommentService, useValue: commentServiceSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PostDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', async () => {
    await setup('42', of(mockPost));
    await fixture.whenStable();
    expect(component).toBeTruthy();
  });

  it('should start with loading true before ngOnInit', async () => {
    postServiceSpy = jasmine.createSpyObj('PostService', ['getById', 'delete']);
    postServiceSpy.getById.and.returnValue(of(mockPost));
    commentServiceSpy = jasmine.createSpyObj('CommentService', ['getComments', 'createComment']);
    commentServiceSpy.getComments.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [PostDetailComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { params: { postId: '42' } } } },
        { provide: PostService, useValue: postServiceSpy },
        { provide: LoginService, useValue: makeLoginService(null) },
        { provide: CommentService, useValue: commentServiceSpy },
      ]
    }).compileComponents();

    const f = TestBed.createComponent(PostDetailComponent);
    expect(f.componentInstance.loading).toBeTrue();
  });

  it('should load post by id from route params', async () => {
    await setup('42', of(mockPost));
    await fixture.whenStable();
    expect(postServiceSpy.getById).toHaveBeenCalledWith(42);
  });

  it('should set post after load', async () => {
    await setup('42', of(mockPost));
    await fixture.whenStable();
    expect(component.post).toEqual(mockPost);
  });

  it('should set loading to false after load', async () => {
    await setup('42', of(mockPost));
    await fixture.whenStable();
    expect(component.loading).toBeFalse();
  });

  it('should set loading to false on HTTP error', async () => {
    await setup('42', throwError(() => new Error('not found')));
    await fixture.whenStable();
    expect(component.loading).toBeFalse();
    expect(component.post).toBeNull();
  });

  it('isAuthor returns true when currentUser matches post author', async () => {
    await setup('42', of(mockPost), 'alice');
    await fixture.whenStable();
    expect(component.isAuthor()).toBeTrue();
  });

  it('isAuthor returns false when currentUser differs from post author', async () => {
    await setup('42', of(mockPost), 'bob');
    await fixture.whenStable();
    expect(component.isAuthor()).toBeFalse();
  });

  it('isAuthor returns false when not logged in', async () => {
    await setup('42', of(mockPost), null);
    await fixture.whenStable();
    expect(component.isAuthor()).toBeFalse();
  });
});
