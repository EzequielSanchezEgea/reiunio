package com.ezequiel.reiunio.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.GameState;
import com.ezequiel.reiunio.repository.GameRepository;
import com.ezequiel.reiunio.repository.LoanRepository;
import com.ezequiel.reiunio.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        log.debug("Finding all games");
        return gameRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Game> findById(Long id) {
        log.debug("Finding game by id: {}", id);
        return gameRepository.findById(id);
    }

    @Override
    @Transactional
    public Game save(Game game) {
        log.debug("Saving game: {}", game.getName());
        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting game by id: {}", id);
        gameRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByName(String name) {
        log.debug("Finding games by name containing: {}", name);
        return gameRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByCategory(String category) {
        log.debug("Finding games by category containing: {}", category);
        return gameRepository.findByCategoryContainingIgnoreCase(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByAvailable(Boolean available) {
        log.debug("Finding games by availability: {}", available);
        return gameRepository.findByAvailable(available);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByState(GameState state) {
        log.debug("Finding games by state: {}", state);
        return gameRepository.findByState(state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByPlayerCount(int playerCount) {
        log.debug("Finding games for {} players", playerCount);
        return gameRepository.findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(
                playerCount, playerCount);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Counting all games");
        return gameRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByAvailable(Boolean available) {
        log.debug("Counting games by availability: {}", available);
        return gameRepository.countByAvailable(available);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<GameState, Long> countByState() {
        log.debug("Counting games by state");
        Map<GameState, Long> result = new HashMap<>();
        
        for (GameState state : GameState.values()) {
            long count = gameRepository.countByState(state);
            result.put(state, count);
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countByCategory() {
        log.debug("Counting games by category");
        List<Game> allGames = gameRepository.findAll();
        
        return allGames.stream()
                .filter(game -> game.getCategory() != null && !game.getCategory().isEmpty())
                .collect(Collectors.groupingBy(
                    Game::getCategory,
                    Collectors.counting()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findMostBorrowed(int limit) {
        log.debug("Finding {} most borrowed games", limit);
        return gameRepository.findMostBorrowedGames(limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Game> findNeverBorrowed() {
        log.debug("Finding games never borrowed");
        return gameRepository.findGamesNeverBorrowed();
    }
}
