import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  UserService,
  type User,
  type PagedUsersResponse,
} from './user-service';

// 模拟 ApiClient
vi.mock('$lib/api/client', () => {
  const mockClient = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  };

  return {
    getDefaultClient: () => mockClient,
  };
});

describe('UserService', () => {
  let userService: UserService;
  let mockApiClient: any;

  beforeEach(() => {
    userService = new UserService();
    mockApiClient = (userService as any).apiClient;

    // 重置所有模拟函数
    vi.resetAllMocks();
  });

  describe('getUsers', () => {
    it('should fetch users with default limit', async () => {
      // 准备
      const mockResponse: PagedUsersResponse = {
        users: [
          {
            id: '1',
            username: 'user1',
            email: 'user1@example.com',
            roles: ['ROLE_USER'],
          },
        ] as User[],
        nextCursor: null,
        hasMore: false,
      };

      mockApiClient.get.mockResolvedValue(mockResponse);

      // 执行
      const result = await userService.getUsers();

      // 验证
      expect(mockApiClient.get).toHaveBeenCalledWith('/auth/users?limit=10');
      expect(result).toEqual(mockResponse);
    });

    it('should fetch users with custom limit and cursor', async () => {
      // 准备
      const mockResponse: PagedUsersResponse = {
        users: [
          {
            id: '2',
            username: 'user2',
            email: 'user2@example.com',
            roles: ['ROLE_USER'],
          },
        ] as User[],
        nextCursor: null,
        hasMore: false,
      };

      mockApiClient.get.mockResolvedValue(mockResponse);

      // 执行
      const result = await userService.getUsers(5, 'some-cursor');

      // 验证
      expect(mockApiClient.get).toHaveBeenCalledWith(
        '/auth/users?limit=5&cursor=some-cursor',
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getUserById', () => {
    it('should fetch user by id', async () => {
      // 准备
      const mockUser: User = {
        id: '1',
        username: 'user1',
        email: 'user1@example.com',
        roles: ['ROLE_USER'],
      };

      mockApiClient.get.mockResolvedValue(mockUser);

      // 执行
      const result = await userService.getUserById('1');

      // 验证
      expect(mockApiClient.get).toHaveBeenCalledWith('/auth/users/1');
      expect(result).toEqual(mockUser);
    });
  });

  describe('updateUser', () => {
    it('should update user', async () => {
      // 准备
      mockApiClient.put.mockResolvedValue({});

      // 执行
      await userService.updateUser('1', {
        email: 'updated@example.com',
        fullName: 'Updated Name',
      });

      // 验证
      expect(mockApiClient.put).toHaveBeenCalledWith('/auth/users/1', {
        email: 'updated@example.com',
        fullName: 'Updated Name',
      });
    });
  });

  describe('deleteUser', () => {
    it('should delete user', async () => {
      // 准备
      mockApiClient.delete.mockResolvedValue({});

      // 执行
      await userService.deleteUser('1');

      // 验证
      expect(mockApiClient.delete).toHaveBeenCalledWith('/auth/users/1');
    });
  });
});
